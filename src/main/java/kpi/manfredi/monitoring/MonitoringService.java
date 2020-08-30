package kpi.manfredi.monitoring;

import kpi.manfredi.tags.TagsHandler;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static java.nio.file.LinkOption.NOFOLLOW_LINKS;
import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.OVERFLOW;

public class MonitoringService implements Runnable {

    private final WatchService watcher;
    private final Map<WatchKey, Path> keys;
    private final boolean recursive;
    private final boolean trace;
    private final TagsHandler tagsHandler;
    private final ArrayList<Path> changedWithinService;
    private final ArrayList<String> ignoreTypes;
    private final DateTimeFormatter timeFormatter;

    @SuppressWarnings("unchecked")
    static <T> WatchEvent<T> cast(WatchEvent<?> event) {
        return (WatchEvent<T>) event;
    }

    /**
     * Register the given directory with the WatchService
     */
    private void register(Path dir) throws IOException {
        WatchKey key = dir.register(watcher, ENTRY_CREATE);
        if (trace) {
            Path prev = keys.get(key);
            if (prev == null) {
                System.out.format("register: %s\n", dir);
            } else {
                if (!dir.equals(prev)) {
                    System.out.format("update: %s -> %s\n", prev, dir);
                }
            }
        }
        keys.put(key, dir);
    }

    /**
     * Register the given directory, and all its sub-directories, with the
     * WatchService.
     */
    private void registerAll(final Path start) throws IOException {
        // register directory and sub-directories
        Files.walkFileTree(start, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs)
                    throws IOException {
                register(dir);
                return FileVisitResult.CONTINUE;
            }
        });
    }

    /**
     * Creates a WatchService and registers the given directory
     */
    public MonitoringService(Path dir, boolean recursive, TagsHandler tagsHandler)
            throws IOException {

        this.recursive = recursive;
        this.keys = new HashMap<>();
        this.tagsHandler = tagsHandler;
        this.changedWithinService = new ArrayList<>();
        this.watcher = FileSystems.getDefault().newWatchService();
        this.timeFormatter = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
        this.ignoreTypes = new ArrayList<>();
        ignoreTypes.add(".crdownload");

        if (recursive) {
            System.out.format("Scanning %s ...\n", dir);
            registerAll(dir);
            System.out.println("Done.");
        } else {
            register(dir);
        }

        // enable trace after initial registration
        this.trace = true;
    }

    /**
     * Process all events for keys queued to the watcher
     */
    @Override
    public void run() {
        System.out.println("Monitoring service is active...\n");
//        for (int i = 1; i < 2; i++) {
        for (; ; ) {

            // wait for key to be signalled
            WatchKey key;
            try {
                key = watcher.take();
                TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException x) {
                return;
            }

            Path dir = keys.get(key);
            if (dir == null) {
                System.err.println("WatchKey not recognized!!");
                continue;
            }

            for (WatchEvent<?> event : key.pollEvents()) {
                WatchEvent.Kind<?> kind = event.kind();

                // No matter what events the key has registered for, it is possible to receive an OVERFLOW even
                if (kind == OVERFLOW) {
                    continue;
                }

                Path name = (Path) event.context();
                Path child = dir.resolve(name);

                // skip event of renaming file in handleFile() method
                if (changedWithinService.contains(child)) {
                    changedWithinService.remove(child);
                    continue;
                }

                String filename = name.getFileName().toString();
                String type = filename.substring(filename.lastIndexOf('.'));
                if (ignoreTypes.contains(type)) {
                    continue; // ignore some types
                }

                if (filename.replace(type, "").matches("^#[a-zA-Z_\\d]+( #[a-zA-Z_\\d]+)+$")) {
                    continue; // file already fine named
                }

                try {
                    File handledFile = tagsHandler.handleFile(child.toFile());
                    changedWithinService.add(handledFile.toPath());
                    System.out.format(timeFormatter.format(LocalDateTime.now()) +
                            "\nNew file: %s\nRenamed to: %s\n\n", child, handledFile.getPath());
                } catch (IOException e) {
                    System.err.println(e.getMessage());
                }

                // if directory is created, and watching recursively, then
                // register it and its sub-directories
                if (recursive && (kind == ENTRY_CREATE)) {
                    try {
                        if (Files.isDirectory(child, NOFOLLOW_LINKS)) {
                            registerAll(child);
                        }
                    } catch (IOException x) {
                        // ignore to keep sample readable
                    }
                }
            }

            // reset key and remove from set if directory no longer accessible
            boolean valid = key.reset();
            if (!valid) {
                keys.remove(key);

                // all directories are inaccessible
                if (keys.isEmpty()) {
                    break;
                }
            }
        }
    }
}
