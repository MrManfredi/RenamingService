package kpi.manfredi;

import javafx.application.Application;
import kpi.manfredi.gui.JavaFxMain;
import kpi.manfredi.monitoring.MonitoringService;

import java.io.IOException;
import java.nio.file.Paths;

public class MainLoader {
    public static void main(String[] args) {
        if (args.length == 0) {
            Application.launch(JavaFxMain.class, args);
        } else if (isHelp(args)) {
            showHelp();
        } else if (isMonitoringService(args)) {
            try {
                if (args.length == 2) {
                    new MonitoringService(Paths.get(args[1]), false).run();
                } else {
                    new MonitoringService(Paths.get(args[2]), true).run();
                }
            } catch (IOException e) {
                System.err.println(e.getMessage());
            }
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

    private static void showHelp() {
        System.out.println("Usage: java -jar <application-name> [-h | -m [-r] <dir>]");
        System.out.println("without param.  - gui application");
        System.out.println("-h              - this help information");
        System.out.println("-m              - directory monitoring service");
        System.out.println("-r              - recursively (sub-folders)");
    }

    /**
     * @param args list of arguments
     * @return {@code true} when the parameters meet the condition of {@code MonitoringService}. Otherwise {@code false}
     */
    public static boolean isMonitoringService(String[] args) {
        boolean result = false;
        if (args.length > 1 && args.length < 4 && args[0].equals("-m") && (args.length == 2 || args[1].equals("-r"))) {
            result = true;
        }
        return result;
    }
}
