package urChatBasic.base;

import java.awt.Font;
import java.io.File;
import java.net.URL;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import javax.swing.JLabel;
import javax.swing.UIManager;
import urChatBasic.backend.Connection;
import urChatBasic.base.capabilities.CapabilityTypes;
import urChatBasic.frontend.DriverGUI;
import urChatBasic.frontend.components.URVersionLabel;

/**
 * Used to store constants that are the same and do not change often. These are things used commonly
 * across the [front,back]end
 *
 * @author goofybud16
 *
 */
public class Constants
{
    public static String UR_VERSION = "v0.4.0";
    public static String URL_SEPARATOR = "/";
    public static final URL RESOURCES_DIR = DriverGUI.class.getResource(URL_SEPARATOR + "images" + URL_SEPARATOR);
    public static final String THEMES_DIR = "themes" + URL_SEPARATOR;
    public static String DIRECTORY_LOGS = "Logs" + URL_SEPARATOR;
    public static Class BACKEND_CLASS;
    public static String BACKEND_CLASS_FULLNAME = "urChatBasic.backend.Connection";
    private static Handler LOGGER_TO_FILE;
    public static Logger LOGGER = Logger.getLogger("Main");
    public static String LOGFILE_NAME = "Errors.log";
    private static final Font DEFAULT_FONT = new Font(new JLabel().getFont().getFamily(), 0, new JLabel().getFont().getSize());

    // Preferences
    public static final Preferences BASE_PREFS = Preferences.userNodeForPackage(DriverGUI.class).node("profiles");

    // Key Strings that are used when saving settings
    public static final String KEY_FIRST_CHANNEL = "first channel name";
    public static final String KEY_FIRST_SERVER = "first server name";
    public static final String KEY_FIRST_PORT = "first server port";
    public static final String KEY_AUTH_TYPE = "authentication type";
    public static final String KEY_PASSWORD_REMEMBER = "remember password";
    public static final String KEY_PASSWORD = "saved password";
    public static final String KEY_USE_TLS = "use tls";
    public static final String KEY_USE_PROXY = "use proxy";
    public static final String KEY_PROXY_HOST = "proxy host";
    public static final String KEY_PROXY_PORT = "proxy port";
    public static final String KEY_NICK_NAME = "nick name";
    public static final String KEY_REAL_NAME = "real name";
    public static final String KEY_TIME_STAMPS = "show time stamps";
    public static final String KEY_TIME_STAMP_FORMAT = "timestamp format";
    public static final String KEY_LAF_NAME = "laf name";
    public static final String KEY_EVENT_TICKER_ACTIVE = "show event ticker";
    public static final String KEY_USERS_LIST_ACTIVE = "show users list";
    public static final String KEY_CLICKABLE_LINKS_ENABLED = "clickable links";
    public static final String KEY_EVENT_TICKER_JOINS_QUITS = "show events in ticker";
    public static final String KEY_MAIN_WINDOW_JOINS_QUITS = "show events in main window";
    public static final String KEY_LOG_CHANNEL_HISTORY = "log channel history";
    public static final String KEY_LOG_SERVER_ACTIVITY = "log server activity";
    public static final String KEY_LIMIT_CHANNEL_LINES = "limit number of channel lines";
    public static final String KEY_AUTO_CONNECT_FAVOURITES = "connect to favourites";
    public static final String KEY_LIMIT_CHANNEL_LINES_COUNT = "limit number of channel lines count";
    public static final String KEY_LIMIT_SERVER_LINES = "limit number of server lines";
    public static final String KEY_LIMIT_SERVER_LINES_COUNT = "limit number of server lines count";
    public static final String KEY_LOG_CLIENT_TEXT = "log client text";
    public static final String KEY_EVENT_TICKER_DELAY = "event ticker delay";
    public static final String KEY_FONT_FAMILY = "font family";
    public static final String KEY_FONT_BOLD = "font bold";
    public static final String KEY_FONT_ITALIC = "font italic";
    public static final String KEY_FONT_SIZE = "font size";
    public static final String KEY_WINDOW_X = "window position x";
    public static final String KEY_WINDOW_Y = "window position y";
    public static final String KEY_WINDOW_WIDTH = "window position width";
    public static final String KEY_WINDOW_HEIGHT = "window position height";

    // Setting defaults
    public static final String DEFAULT_FIRST_CHANNEL = "#linux";
    public static final String DEFAULT_FIRST_SERVER = "irc.libera.chat";
    public static final String DEFAULT_FIRST_PORT = "6697";
    public static final String DEFAULT_AUTH_TYPE = CapabilityTypes.NONE.toString();
    public static final String DEFAULT_PASSWORD = "";
    public static final Boolean DEFAULT_PASSWORD_REMEMBER = false;
    public static final Boolean DEFAULT_USE_TLS = true;
    public static final Boolean DEFAULT_USE_PROXY = false;
    public static final String DEFAULT_PROXY_HOST = "";
    public static final String DEFAULT_PROXY_PORT = "";
    public static final String DEFAULT_NICK_NAME = "urChatClient";
    public static final String DEFAULT_REAL_NAME = "urChatClient";
    public static final Boolean DEFAULT_TIME_STAMPS = true;
    public static final String DEFAULT_TIME_STAMP_FORMAT = "[HHmm]";
    public static final String DEFAULT_LAF_NAME = UIManager.getSystemLookAndFeelClassName();
    public static final Boolean DEFAULT_EVENT_TICKER_ACTIVE = true;
    public static final Boolean DEFAULT_CLICKABLE_LINKS_ENABLED = true;
    public static final Boolean DEFAULT_USERS_LIST_ACTIVE = true;
    public static final Boolean DEFAULT_EVENT_TICKER_JOINS_QUITS = true;
    public static final Boolean DEFAULT_MAIN_WINDOW_JOINS_QUITS = true;
    public static final Boolean DEFAULT_LOG_CHANNEL_HISTORY = false;
    public static final Boolean DEFAULT_LOG_SERVER_ACTIVITY = false;
    public static final Boolean DEFAULT_AUTO_CONNECT_FAVOURITES = false;
    public static final Boolean DEFAULT_LIMIT_CHANNEL_LINES = true;
    public static final String DEFAULT_LIMIT_CHANNEL_LINES_COUNT = "500";
    public static final Boolean DEFAULT_LIMIT_SERVER_LINES = true;
    public static final String DEFAULT_LIMIT_SERVER_LINES_COUNT = "500";
    public static final Boolean DEFAULT_LOG_CLIENT_TEXT = true;
    public static final Font DEFAULT_FONT_GENERAL = DEFAULT_FONT;
    public static final int DEFAULT_EVENT_TICKER_DELAY = 10;
    public static final int DEFAULT_WINDOW_X = 0;
    public static final int DEFAULT_WINDOW_Y = 0;
    public static final int DEFAULT_WINDOW_WIDTH = 640;
    public static final int DEFAULT_WINDOW_HEIGHT = 480;

    // Message Handler
    public static final char CHANNEL_DELIMITER = '#';
    public static final char CTCP_DELIMITER = '\001';
    public static final char SPACES_AHEAD_DELIMITER = ':';
    public static final int MESSAGE_LIMIT = 510;
    public static final String END_MESSAGE = "\r\n";
    // We 'must' match against http(s) in order to define the correct protocol to be used
    public static final String URL_REGEX = "((http:\\/\\/|https:\\/\\/)(www.)?(([a-zA-Z0-9-]){2,}\\.){1,4}([a-zA-Z]){2,6}(\\/([a-zA-Z-_\\/\\.0-9#:?=&;,]*)?)?)";
    public static final String CHANNEL_REGEX = "(?:^|\s)(#([^\s,]+)(?!,))(?:$|\s)";
    // Used to identify a message to be printed from the Event ticker
    // like a "user joins room" type message
    public static final String EVENT_USER = "****";

    // Main text area
    public static final int MAIN_WIDTH = 500;
    public static final int MAIN_HEIGHT = 400;

    public enum Size {
        LARGE,
        MEDIUM,
        SMALL,
        NONE
    }

    /**
     * Used to initialize some values that may throw exceptions.
     */
    public static void init()
    {
        URVersionLabel.setVersion();

        try
        {
            File logDir = new File(DIRECTORY_LOGS);
            if (!logDir.exists())
            {
                logDir.mkdir();
            }
            File logFile = new File(Constants.DIRECTORY_LOGS, LOGFILE_NAME);
            if (!logFile.exists())
            {
                logFile.createNewFile();
            }
            LOGGER_TO_FILE = new FileHandler(logFile.getAbsolutePath(), true);
            LOGGER.addHandler(LOGGER_TO_FILE);
        } catch (Exception e)
        {
            LOGGER.log(Level.WARNING, "Failed to create LOGGER_TO_FILE: " + e.getLocalizedMessage());
        }
        try
        {
            new Object().getClass();
            BACKEND_CLASS = Class.forName(BACKEND_CLASS_FULLNAME);
        } catch (Exception e)
        {
            LOGGER.log(Level.WARNING, "BACKEND_CLASS failed to load! Defaulting to default implementation");
            BACKEND_CLASS = Connection.class;
        }
    }
}
