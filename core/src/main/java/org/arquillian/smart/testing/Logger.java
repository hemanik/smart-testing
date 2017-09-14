package org.arquillian.smart.testing;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;

import static java.lang.String.format;
import static org.arquillian.smart.testing.Configuration.SMART_TESTING_DEBUG;
import static org.codehaus.plexus.logging.Logger.LEVEL_DEBUG;

@Component(role = LifeCycleDebugLogger.class)
public class Logger extends LifeCycleDebugLogger {

    @Requirement
    private static org.codehaus.plexus.logging.Logger mavenLogger;

    private static final String PREFIX = " [Smart Testing Extension] ";

    private Logger() {

    }

    private Logger(org.codehaus.plexus.logging.Logger mavenLogger) {
        Logger.mavenLogger = mavenLogger;
    }

    public static Logger getLogger() {
        return new Logger(mavenLogger);
    }

    /**
     * Will format the given message with the given arguments and prints it on standard output with the prefix:
     * "[Smart Testing Extension]"
     *
     * @param msg
     *     Message to print
     * @param args
     *     arguments to use for formatting the given message
     */
    public void info(String msg, Object... args) {
        mavenLogger.info(getFormattedMsg(msg, args));
    }

    /**
     * Will format the given message with the given arguments and prints it on error output with the prefix:
     * "[Smart Testing Extension]"
     *
     * @param msg
     *     Message to print
     * @param args
     *     arguments to use for formatting the given message
     */
    public void warn(String msg, Object... args) {
        mavenLogger.warn(getFormattedMsg(msg, args));
    }

    /**
     * Will format the given message with the given arguments and prints it on standard output with the prefix:
     * "[Smart Testing Extension]", if debug mode is enabled.
     *
     * @param msg
     *     The string message (or a key in the message catalog)
     * @param args
     *     arguments to the message
     */
    public void debug(String msg, Object... args) {
        mavenLogger.debug(getFormattedMsg(msg, args));
    }

    /**
     * Will format the given message with the given arguments and prints it on standard output with the prefix:
     * "[Smart Testing Extension]", if debug mode is enabled.
     *
     * @param msg
     *     The string message (or a key in the message catalog)
     * @param args
     *     arguments to the message
     */
    public void error(String msg, Object... args) {
        mavenLogger.error(getFormattedMsg(msg, args));
    }

    /**
     * Will format the given message with the given arguments and prints it on standard output with the prefix:
     * "[Smart Testing Extension]", if debug mode is enabled.
     *
     * @param msg
     *     The string message (or a key in the message catalog)
     * @param args
     *     arguments to the message
     */
    public void fatalError(String msg, Object... args) {
        mavenLogger.fatalError(getFormattedMsg(msg, args));
    }

    public boolean isDebugLogLevelEnabled() {
        return mavenLogger.isDebugEnabled();
    }

    public void enableDebugLogLevel() {
        if (Boolean.valueOf(System.getProperty(SMART_TESTING_DEBUG, "false"))) {
            mavenLogger.setThreshold(LEVEL_DEBUG);
        }
    }

    private String getFormattedMsg(String msg, Object... args) {
        if (args != null && args.length > 0) {
            msg = format(msg, args);
        }
        return PREFIX + msg;
    }
}
