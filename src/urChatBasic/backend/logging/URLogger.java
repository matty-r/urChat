package urChatBasic.backend.logging;

import static urChatBasic.base.Constants.LOGGER;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URISyntaxException;
import java.util.Map;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.Filter.Result;
import org.apache.logging.log4j.core.appender.FileAppender;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.filter.MarkerFilter;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;
import urChatBasic.base.Constants;
import urChatBasic.base.IRCChannelBase;
import urChatBasic.frontend.DriverGUI;

public class URLogger
{
    private static final String LOG4J_CONFIG_FILE = Constants.RESOURCES_PATH + "log4j2.xml";

    private static LoggerContext context;
    static Configuration currentConfig;

    public static void init () throws IOException, URISyntaxException
    {
        // System.out.println("LOG CONFIG: "+ LOG4J_CONFIG_FILE);
        File logDir = new File(Constants.DIRECTORY_LOGS);
        if (!logDir.exists())
        {
            logDir.mkdir();
        }



        // System.setProperty("log4j2.debug", "true");
        System.setProperty("log4j2.configurationFile", DriverGUI.class.getResource(LOG4J_CONFIG_FILE).toURI().toString());

        LOGGER = LoggerFactory.getLogger("urchat");

        Logger testLog = getLogger(LOGGER.getName(), Logger.class);

        // Initialize the logger context using the ConfigurationSource
        context = testLog.getContext();
        currentConfig = context.getConfiguration();
    }

    public static <T> T getLogger(final String loggerName, final Class<T> loggerClass) {
        final org.slf4j.Logger logger = LoggerFactory.getLogger(loggerName);
        try {
            final Class<? extends org.slf4j.Logger> loggerIntrospected = logger.getClass();
            final Field fields[] = loggerIntrospected.getDeclaredFields();
            for (int i = 0; i < fields.length; i++) {
                final String fieldName = fields[i].getName();
                if (fieldName.equals("logger")) {
                    fields[i].setAccessible(true);
                    return loggerClass.cast(fields[i].get(logger));
                }
            }
        } catch (final Exception e) {
            logger.error(e.getMessage());
        }
        return null;
    }

    public static Marker getMarker (String markerName)
    {
        return MarkerFactory.getMarker(markerName);
    }

    // private static String loadConfigFile (String fileName) throws IOException
    // {
    //     // Load the configuration file content from the classpath
    //     try (InputStream inputStream = DriverGUI.class.getResourceAsStream(fileName))
    //     {
    //         if (inputStream == null)
    //         {
    //             throw new IOException("Configuration file not found: " + fileName);
    //         }

    //         String configFileString = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
    //         return configFileString;
    //     }
    // }

     /**
     * Get the path for the logfile associated with the given markerName.
     *
     * @param markerName The markerName associated with the logfile.
     * @return The path for the associated logfile, or null if not found.
     */
    public static String getLogFilePath(String markerName) {
        // Get the root LoggerConfig
        Configuration rootLoggerConfig = currentConfig;
        if (rootLoggerConfig != null) {
            // Find the appender associated with the given markerName
            Map<String, Appender> appenders = rootLoggerConfig.getAppenders();
            String appenderName = markerName + "Appender";
            Appender appender = appenders.get(appenderName);
            if (appender instanceof FileAppender) {
                // If the appender is a FileAppender, return its file name
                FileAppender fileAppender = (FileAppender) appender;
                return fileAppender.getFileName();
            }
        }
        // Return null if the logfile for the given markerName is not found
        return null;
    }

    public static void logChannelComms (IRCChannelBase ircChannel, String message)
    {

        LOGGER.info(getMarker(ircChannel.getMarker()), message);
    }

    /**
     * Copy the default FileAppender and create a new one for the new Marker
     *
     * @param markerName
     */
    public static void addChannelMarker (String markerName)
    {
        String appenderName = markerName + "Appender"; // Replace with your appender name
        String loggerName = markerName; // Replace with your logger name

        // Get the root LoggerConfig
        Configuration rootLoggerConfig = currentConfig;
        Logger rootLogger;

        if (currentConfig.getLoggerContext() != null)
        {
            rootLogger = currentConfig.getLoggerContext().getRootLogger();
        } else
        {
            return;
        }

        FileAppender existingFileAppender = (FileAppender) rootLoggerConfig.getAppenders().get("BaseChannelAppender");

        // Create a new FileAppender using the existingFileAppender as a base
        FileAppender.Builder<?> newAppenderBuilder =
                FileAppender.newBuilder().setName(appenderName).withFileName("Logs/" + loggerName + ".log").setLayout(existingFileAppender.getLayout());

        // Add MarkerFilter for the specified markerName
        MarkerFilter acceptNewMarker = MarkerFilter.createFilter(markerName, Result.ACCEPT, Result.DENY);
        newAppenderBuilder.setFilter(acceptNewMarker);

        Appender newFileAppender = newAppenderBuilder.build();
        newFileAppender.start();

        // Add the newFileAppender to the configuration
        currentConfig.addAppender(newFileAppender);
        currentConfig.addLoggerAppender(rootLogger, newFileAppender);

        // Update the context with the new configuration
        context.updateLoggers(currentConfig);
    }

}
