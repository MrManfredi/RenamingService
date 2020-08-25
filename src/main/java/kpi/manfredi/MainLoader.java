package kpi.manfredi;

import javafx.application.Application;
import kpi.manfredi.gui.JavaFxMain;
import kpi.manfredi.monitoring.MonitoringService;

public class MainLoader {
    public static void main(String[] args) {
        if (args.length == 0) {
            Application.launch(JavaFxMain.class, args);
        } else if (args.length == 1) {
            switch (args[0]) {
                case "-h":
                    showHelp();
                    break;
                case "-m":
                    new MonitoringService().run();
                    break;
                default:
                    showError();
            }
        } else {
            showError();
        }

    }

    private static void showError() {
        System.err.println("Wrong list of arguments.");
        showHelp();
    }

    private static void showHelp() {
        System.out.println("Usage: java -jar <application-name> [-h | -m]");
        System.out.println("without param. - gui application");
        System.out.println("-h  - this help information");
        System.out.println("-m  - directory monitoring service");
    }
}
