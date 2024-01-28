package urChatBasic.backend.logging;

import static urChatBasic.base.Constants.LOGGER;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.Filter.Result;
import org.apache.logging.log4j.core.appender.FileAppender;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.ConfigurationSource;
import org.apache.logging.log4j.core.config.Configurator;
import org.apache.logging.log4j.core.filter.MarkerFilter;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;
import urChatBasic.base.Constants;
import urChatBasic.frontend.DriverGUI;

public class URLogger
{
    private static final String LOG4J_CONFIG_FILE = Constants.RESOURCES_PATH + "log4j2.xml";

    private static LoggerContext context;
    static Configuration currentConfig;

    // final static String commsMarker = "Comms Marker";

    public static void init () throws IOException
    {
        // Load the log4j2.xml configuration file content
        String configContent = loadConfigFile(LOG4J_CONFIG_FILE);

        // Create a ConfigurationSource from the configuration file content
        ConfigurationSource source = new ConfigurationSource(new ByteArrayInputStream(configContent.getBytes(StandardCharsets.UTF_8)));

        // Initialize the logger context using the ConfigurationSource
        context = Configurator.initialize(null, source);


        currentConfig = context.getConfiguration();


        LOGGER = LoggerFactory.getLogger("urchat");
    }

    public static Marker getMarker (String markerName)
    {
        return MarkerFactory.getMarker(markerName);
    }

    private static String loadConfigFile (String fileName) throws IOException
    {
        // Load the configuration file content from the classpath
        try (InputStream inputStream = DriverGUI.class.getResourceAsStream(fileName))
        {
            if (inputStream == null)
            {
                throw new IOException("Configuration file not found: " + fileName);
            }
            return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    public static void logChannelComms (String channelName, String message)
    {

        LOGGER.info(getMarker(channelName), message);
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
        Logger rootLogger = currentConfig.getLoggerContext().getRootLogger();

        FileAppender existingFileAppender = (FileAppender) rootLoggerConfig.getAppenders().get("BaseChannelAppender");

        // Create a new FileAppender using the existingFileAppender as a base
        FileAppender.Builder<?> newAppenderBuilder = FileAppender.newBuilder().setName(appenderName).withFileName("Logs/" + loggerName + ".log")
                .setLayout(existingFileAppender.getLayout());

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
