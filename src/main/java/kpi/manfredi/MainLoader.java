package kpi.manfredi;

import javafx.application.Application;
import kpi.manfredi.gui.JavaFxMain;
import kpi.manfredi.monitoring.FilenameHandler;
import kpi.manfredi.monitoring.MonitoringService;
import kpi.manfredi.scanning.TagsScanner;
import kpi.manfredi.tags.TagsCustodian;
import kpi.manfredi.tags.map.TagsMap;
import kpi.manfredi.utils.FileManipulation;
import kpi.manfredi.utils.WrongArgumentsException;

import javax.xml.bind.JAXBException;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class MainLoader {

    public static void main(String[] args) {
        if (args.length == 0) {
            Application.launch(JavaFxMain.class, args);
        } else if (isHelp(args)) {
            showHelp();
        } else if (args[0].equals("-s")) {
            runTagsScanner(args);
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
        File file;
        try {
            file = FileManipulation.getResourceFile("/help.txt");
        } catch (FileNotFoundException e) {
            System.err.println("Help file is not found!");
            return;
        }

        try (FileReader fr = new FileReader(file)) {
            BufferedReader br = new BufferedReader(fr);
            String line;
            while ((line = br.readLine()) != null) {
                System.out.println(line);
            }
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
    }

    /**
     * This method is used to invoke tags analyzer
     *
     * @param args input arguments
     */
    private static void runTagsScanner(String[] args) {
        try {
            System.out.println("\nTags scanner is active...\n");
            File file = TagsScanner.scan(args);
            if (file.exists()) {
                System.out.println("Tags was written into " + file.getName());
            } else {
                System.out.println("Tags not found.");
            }
        } catch (IOException | WrongArgumentsException | JAXBException e) {
            System.err.println(e.getMessage());
            System.exit(-1);
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
            FilenameHandler filenameHandler = new FilenameHandler(tagsMap);
            new MonitoringService(dir, recursive, filenameHandler).run();
        } catch (IOException | IllegalAccessException | JAXBException e) {
            System.err.println(e.getMessage());
        }
    }
}
