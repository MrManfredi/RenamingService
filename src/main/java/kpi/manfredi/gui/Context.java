package kpi.manfredi.gui;

import javafx.stage.Stage;

import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

/**
 * This class is used to contain configurations to manage application
 */
public class Context {

    private final List<Locale> availableLocales;

    private Stage primaryStage;
    private Locale currentLocale;

    private static Context instance;

    /**
     * This method is used to return instance of {@code Context} class
     *
     * @return instance of {@code Context} class
     */
    public static Context getInstance() {
        if (instance == null) {
            instance = new Context();
        }
        return instance;
    }

    private Context() {
        availableLocales = new LinkedList<>();
        availableLocales.add(new Locale("eng"));
        availableLocales.add(new Locale("ukr"));
        availableLocales.add(new Locale("rus"));
        currentLocale = new Locale("eng");  // default locale
    }

    /**
     * This method is used to set primary stage of application
     *
     * @param primaryStage primary stage
     */
    void setPrimaryStage(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }

    /**
     * This method is used to return primary stage of application
     *
     * @return primary stage of application
     */
    public Stage getPrimaryStage() {
        return primaryStage;
    }

    /**
     * This method is used to return available locales
     *
     * @return available locales
     */
    public List<Locale> getAvailableLocales() {
        return availableLocales;
    }

    /**
     * This method is used to return current locale setting
     *
     * @return current locale
     */
    public Locale getCurrentLocale() {
        return currentLocale;
    }

    /**
     * This method is used to set current locale setting
     *
     * @param currentLocale locale to set
     */
    public void setCurrentLocale(Locale currentLocale) {
        this.currentLocale = currentLocale;
    }
}
