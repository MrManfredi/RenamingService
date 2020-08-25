package kpi.manfredi.gui;

/**
 * This enumeration is used to contain path to each screen in application
 *
 * @author manfredi
 */
public enum Screen {
    ITERATIVE_RENAMING("/screens/IterativeRenamingScreen.fxml"),
    PROCESSING_ENVIRONMENT("/screens/ProcessingEnvironmentScreen.fxml");

    private final String path;

    Screen(String path) {
        this.path = path;
    }

    /**
     * This method is used to return the path to the screen
     *
     * @return path to the screen
     */
    public String getPath() {
        return path;
    }
}
