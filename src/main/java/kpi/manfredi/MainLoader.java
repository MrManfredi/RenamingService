package kpi.manfredi;

import javafx.application.Application;
import kpi.manfredi.gui.JavaFxMain;
import kpi.manfredi.monitoring.MonitoringService;
import kpi.manfredi.tags.TagsAdapter;
import kpi.manfredi.tags.TagsAnalyzer;
import kpi.manfredi.tags.TagsCustodian;
import kpi.manfredi.tags.TagsHandler;
import kpi.manfredi.tags.map.TagsMap;

import javax.xml.bind.JAXBException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class MainLoader {

    public static void main(String[] args) {
        if (args.length == 0) {
            Application.launch(JavaFxMain.class, args);
        } else if (isHelp(args)) {
            showHelp();
        } else if (isTagsAnalyzer(args)) {
            runTagsAnalyzer(args);
        } else if (isMonitoringService(args)) {
            runMonitoringService(args);
        } else {
            showError();
        }

    }

    private static void showError() {
        System.err.println("Wrong list of arguments.");
        showHelp();
        System.exit(-1);
    }

    private static boolean isHelp(String[] args) {
        return args.length == 1 && args[0].equals("-h");
    }

    /**
     * This method is used to show help information about application API
     */
    private static void showHelp() {
        System.out.println("Usage: java -jar <application-name> [-h | -m [-r] <dir> <file-with-tags> | " +
                "-a [-r] <dir> [<file-save-into>]]");
        System.out.println("without param.  - gui application");
        System.out.println("-a              - analyze directory and collect tags");
        System.out.println("-h              - this help information");
        System.out.println("-m              - directory monitoring service");
        System.out.println("-r              - recursively (sub-folders)");
    }

    /**
     * This method is used to check if arguments meet the condition of {@code TagsAnalyzer}.
     * <br><br>
     * Valid input parameters: -a [-r] &lt;dir&gt; [&lt;file-save-into&gt;]
     *
     * @param args list of arguments
     * @return {@code true} when the parameters meet the condition of {@code TagsAnalyzer}. Otherwise {@code false}
     */
    private static boolean isTagsAnalyzer(String[] args) {
        boolean result;
        if (args.length < 2 || !args[0].equals("-a")) {
            result = false;
        } else if (args.length == 2 && Files.exists(Paths.get(args[1]))) {
            result = true;
        } else result = args.length < 5 && args[1].equals("-r") && Files.exists(Paths.get(args[2]));

        return result;
    }

    /**
     * This method is used to invoke tags analyzer
     *
     * @param args input arguments
     */
    private static void runTagsAnalyzer(String[] args) {
        System.out.println("Tags analyzer is active...\n");
        List<String> tags;
        if (args.length == 2 || !args[1].equals("-r")) {
            tags = TagsAnalyzer.getTagsFromDirectory(Paths.get(args[1]), false);
        } else {
            tags = TagsAnalyzer.getTagsFromDirectory(Paths.get(args[2]), true);
        }

        if (tags != null) {
            if (args.length == 2 || (args.length == 3 && args[1].equals("-r"))) {
                System.out.println("------Result set------");
                for (String item : tags) {
                    System.out.println(item);
                }
            } else {
                try {
                    TagsCustodian.saveTags(TagsAdapter.getMapper(tags), args[3]);
                } catch (FileNotFoundException | JAXBException e) {
                    System.err.println(e.getMessage());
                    System.exit(-1);
                }
                System.out.println("Tags was written into " + args[3]);
            }
        }
    }

    /**
     * This method is used to check if arguments meet the condition of {@code MonitoringService}.
     * <br><br>
     * Valid input parameters: -m [-r] &lt;dir&gt; &lt;file-with-tags&gt;
     *
     * @param args list of arguments
     * @return {@code true} when the parameters meet the condition of {@code MonitoringService}. Otherwise {@code false}
     */
    public static boolean isMonitoringService(String[] args) {
        boolean result;

        if (args.length < 3 || !args[0].equals("-m")) {
            result = false;
        } else if (args[1].equals("-r")) {
            result = args.length == 4 && Files.exists(Paths.get(args[2])) && Files.exists(Paths.get(args[3]));
        } else {
            result = args.length == 3 && Files.exists(Paths.get(args[1])) && Files.exists(Paths.get(args[2]));
        }

        return result;
    }

    /**
     * This method is used to invoke monitoring service
     *
     * @param args input arguments
     */
    private static void runMonitoringService(String[] args) {
        boolean recursive;
        Path dir;
        File tagsFile;

        if (args.length == 3) {
            recursive = false;
            dir = Paths.get(args[1]);
            tagsFile = new File(args[2]);
        } else {
            recursive = true;
            dir = Paths.get(args[2]);
            tagsFile = new File(args[3]);
        }
        try {
            TagsMap tagsMap = (TagsMap) TagsCustodian.getTags(tagsFile, TagsMap.class);
            TagsHandler tagsHandler = new TagsHandler(tagsMap);
            new MonitoringService(dir, recursive, tagsHandler).run();
        } catch (IOException | IllegalAccessException | JAXBException e) {
            System.err.println(e.getMessage());
        }
    }
}
