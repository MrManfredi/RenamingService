package kpi.manfredi.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * This class is used to provide methods to work with localized messages
 *
 * @author manfredi
 */
public class MessageUtil {
    private final static Logger logger = LoggerFactory.getLogger(MessageUtil.class);
    private final static String RESOURCE_NAME = "i18n.messages";

    /**
     * This method is used to return current locale setting
     *
     * @return current locale
     */
    private static Locale getCurrentLocale() {
        // todo read locale from config file
        return new Locale("eng");   // eng, ukr, rus
    }

    /**
     * This method is used to return resource bundle with current locale
     *
     * @return resource bundle with current locale. Otherwise resource bundle with default locale.
     */
    private static ResourceBundle getResource() {
        return ResourceBundle.getBundle(RESOURCE_NAME, getCurrentLocale());
    }

    /**
     * This method is used to return localized message by it`s {@code key}
     *
     * @param key message key
     * @return localized message
     */
    public static String getMessage(String key) {
        String message;
        try {
            message = getResource().getString(key);
        } catch (MissingResourceException e) {
            logger.error("{}", e.getMessage());
            message = key;
        }
        return message;
    }

    /**
     * This method is used to format localized message by it`s {@code key} using {@code args} as arguments list
     *
     * @param key  message key by which the corresponding message will be found
     * @param args list of arguments used in the message
     * @return formatted localized message
     */
    public static String formatMessage(String key, Object... args) {
        MessageFormat messageFormat = new MessageFormat(getMessage(key), getResource().getLocale());
        return messageFormat.format(args);
    }

}
