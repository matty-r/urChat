package urChatBasic.backend.logging;

import static urChatBasic.base.Constants.LOGGER;
import java.io.IOException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.Filter.Result;
import org.apache.logging.log4j.core.appender.FileAppender;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.Configurator;
import org.apache.logging.log4j.core.filter.MarkerFilter;
import org.apache.logging.log4j.core.layout.PatternLayout;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;
import urChatBasic.base.Constants;

public class URLogger
{
    private static final LoggerContext context = (LoggerContext) LogManager.getContext(false);
    static Configuration currentConfig;

    // final static String commsMarker = "Comms Marker";

    public static void init () throws IOException
    {
        LoggerContext loggingContext = Configurator.initialize(null, Constants.RESOURCES_DIR + "log4j2.xml");
        currentConfig = loggingContext.getConfiguration();


        LOGGER = LoggerFactory.getLogger("urchat");
        // addAppenderAndLoggerForMarker(commsMarker);
        // LOGGER.info(getMarker(commsMarker), "Init Comms Marker");
    }

    public static Marker getMarker (String markerName)
    {
        return MarkerFactory.getMarker(markerName);
    }

    public static void logChannelComms (String channelName, String message)
    {


        LOGGER.info(getMarker(channelName), message);
    }

    /**
     * Copy the default FileAppender and create a new one for the new Marker
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

        PatternLayout existingLayout = (PatternLayout) existingFileAppender.getLayout();

        // Create a new FileAppender using the existingFileAppender as a base
        FileAppender.Builder<?> newAppenderBuilder =
                FileAppender.newBuilder().setName(appenderName)
                        .withFileName("Logs/"+loggerName+".log")
                        // .withAppend(existingFileAppender.isAppend())
                        .setLayout(existingLayout);

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
