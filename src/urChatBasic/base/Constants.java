package urChatBasic.base;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.io.File;
import java.net.URL;
import java.util.Random;
import java.util.prefs.Preferences;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.text.StyleConstants;
import org.slf4j.Logger;
import urChatBasic.backend.Connection;
import urChatBasic.backend.logging.URLogger;
import urChatBasic.backend.utils.URStyle;
import urChatBasic.base.capabilities.CapabilityTypes;
import urChatBasic.base.proxy.ProxyTypes;
import urChatBasic.frontend.DriverGUI;
import urChatBasic.frontend.components.URVersionLabel;
import urChatBasic.frontend.utils.URColour;

/**
 * Used to store constants that are the same and do not change often. These are things used commonly across the [front,back]end
 *
 */
public class Constants
{
    public static String UR_VERSION = "v0.6.0";
    public static String APP_NAME = "urChatClient" + UR_VERSION;
    public static String URL_SEPARATOR = "/";
    public static final String RESOURCES_PATH = URL_SEPARATOR + "resources" + URL_SEPARATOR;
    public static final URL RESOURCES_DIR = DriverGUI.class.getResource(RESOURCES_PATH);
    public static final URL IMAGES_DIR = DriverGUI.class.getResource(RESOURCES_PATH + "images" + URL_SEPARATOR);
    public static final String THEMES_DIR = "themes" + URL_SEPARATOR;
    public static String DIRECTORY_LOGS = "Logs" + URL_SEPARATOR;
    public static Class BACKEND_CLASS;
    public static String BACKEND_CLASS_FULLNAME = "urChatBasic.backend.Connection";

    public static Logger LOGGER;
    private static final JLabel DEFAULT_LABEL = new JLabel();
    // DEFAULT_FONT should be
    private static final Font DEFAULT_FONT = new Font(DEFAULT_LABEL.getFont().getFamily(), 0, DEFAULT_LABEL.getFont().getSize());

    /**
     * For use with the UIManager.
     */
    public static final String DEFAULT_FONT_STRING = "Label.font";
    public static final URStyle DEFAULT_STYLE = new URStyle("", UIManager.getFont(DEFAULT_FONT_STRING));
    /**
     * For use with the UIManager.
     */
    public static final String DEFAULT_FOREGROUND_STRING = "TextArea.foreground";
    /**
     * For use with the UIManager.
     */
    public static final String DEFAULT_BACKGROUND_STRING = "TextArea.background";

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
    public static final String KEY_PROXY_TYPE = "proxy type";
    public static final String KEY_PROXY_HOST = "proxy host";
    public static final String KEY_PROXY_PORT = "proxy port";
    public static final String KEY_NICK_NAME = "nick name";
    public static final String KEY_REAL_NAME = "real name";
    public static final String KEY_TIME_STAMPS = "show time stamps";
    public static final String KEY_TIME_STAMP_FORMAT = "timestamp format";
    public static final String KEY_NICK_FORMAT = "nick format";
    public static final String KEY_LAF_NAME = "laf name";
    public static final String KEY_SHOW_TAB_ICON = "show tab icon";
    public static final String KEY_EVENT_TICKER_ACTIVE = "show event ticker";
    public static final String KEY_USERS_LIST_ACTIVE = "show users list";
    public static final String KEY_CLICKABLE_LINKS_ENABLED = "clickable links";
    public static final String KEY_EVENT_TICKER_JOINS_QUITS = "show events in ticker";
    public static final String KEY_MAIN_WINDOW_JOINS_QUITS = "show events in main window";
    public static final String KEY_LOG_CHANNEL_ACTIVITY = "log channel history";
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
    public static final String KEY_FONT_UNDERLINE = "font underline";
    public static final String KEY_FONT_SIZE = "font size";
    public static final String KEY_FONT_FOREGROUND = "font foreground";
    public static final String KEY_FONT_BACKGROUND = "font background";
    public static final String KEY_WINDOW_X = "window position x";
    public static final String KEY_WINDOW_Y = "window position y";
    public static final String KEY_WINDOW_WIDTH = "window position width";
    public static final String KEY_WINDOW_HEIGHT = "window position height";
    public static final String KEY_DEFAULT_PROFILE_NAME = "default profile";

    // Setting defaults
    public static final String DEFAULT_PROFILE_NAME = "Default";
    public static final String DEFAULT_FIRST_CHANNEL = "#urchat";
    public static final String DEFAULT_FIRST_SERVER = "irc.libera.chat";
    public static final String DEFAULT_FIRST_PORT = "6697";
    public static final String DEFAULT_AUTH_TYPE = CapabilityTypes.NONE.toString();
    public static final String DEFAULT_PASSWORD = "";
    public static final Boolean DEFAULT_PASSWORD_REMEMBER = false;
    public static final Boolean DEFAULT_USE_TLS = true;
    public static final String DEFAULT_PROXY_TYPE = ProxyTypes.NONE.toString();
    public static final String DEFAULT_PROXY_HOST = "";
    public static final String DEFAULT_PROXY_PORT = "";
    public static final String DEFAULT_NICK_NAME = "urChat"+new Random().nextInt(100, 999);
    public static final String DEFAULT_REAL_NAME = DEFAULT_NICK_NAME;
    public static final Boolean DEFAULT_TIME_STAMPS = true;
    public static final String DEFAULT_TIME_STAMP_FORMAT = "[HHmm]";
    public static final String DEFAULT_NICK_FORMAT = "<nick>";
    public static final String DEFAULT_LAF_NAME = UIManager.getSystemLookAndFeelClassName();
    public static final Boolean DEFAULT_SHOW_TAB_ICON = true;
    public static final Boolean DEFAULT_EVENT_TICKER_ACTIVE = true;
    public static final Boolean DEFAULT_CLICKABLE_LINKS_ENABLED = true;
    public static final Boolean DEFAULT_USERS_LIST_ACTIVE = true;
    public static final Boolean DEFAULT_EVENT_TICKER_JOINS_QUITS = true;
    public static final Boolean DEFAULT_MAIN_WINDOW_JOINS_QUITS = true;
    public static final Boolean DEFAULT_LOG_CHANNEL_ACTIVITY = true;
    public static final Boolean DEFAULT_LOG_SERVER_ACTIVITY = true;
    public static final Boolean DEFAULT_AUTO_CONNECT_FAVOURITES = false;
    public static final Boolean DEFAULT_LIMIT_CHANNEL_LINES = true;
    public static final String DEFAULT_LIMIT_CHANNEL_LINES_COUNT = "500";
    public static final Boolean DEFAULT_LIMIT_SERVER_LINES = true;
    public static final String DEFAULT_LIMIT_SERVER_LINES_COUNT = "500";
    public static final Boolean DEFAULT_LOG_CLIENT_TEXT = true;
    public static final Font DEFAULT_FONT_GENERAL = DEFAULT_FONT;
    public static final String DEFAULT_FONT_FOREGROUND = URColour.hexEncode(DEFAULT_LABEL.getForeground());
    public static final String DEFAULT_FONT_BACKGROUND = URColour.hexEncode(DEFAULT_LABEL.getBackground());
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
    // like a "user joins channel" type message
    public static final String EVENT_USER = "****";

    // Main text area
    public static final int MAIN_WIDTH = 500;
    public static final int MAIN_HEIGHT = 400;

    static {
        // Init the LOGGER stuff
        try
        {
            URLogger.init();
        } catch (Exception e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public enum Size
    {
        LARGE, MEDIUM, SMALL, CUSTOM, NONE;

        Dimension sizeDimension;

        public Size customSize (int width, int height)
        {
            sizeDimension = new Dimension(width, height);
            return CUSTOM;
        }

        public Dimension getDimension ()
        {
            return sizeDimension;
        }

        public void setComponentSize (Component component)
        {
            switch (this) {
                case LARGE:
                    if(component instanceof JTextField)
                        ((JTextField)component).setColumns(12);
                    break;
                case MEDIUM:
                    if(component instanceof JTextField)
                        ((JTextField)component).setColumns(8);
                    break;
                case SMALL:
                    if(component instanceof JTextField)
                        ((JTextField)component).setColumns(4);
                    break;
                case CUSTOM:
                    component.setPreferredSize(getDimension());
                default:
                    break;
            }
        }
    }

    public enum Placement
    {
        TOP, RIGHT, BOTTOM, LEFT, DEFAULT
    }

    public enum EventType
    {
        CREATE, DELETE, RENAME, CHANGE
    }

    // TODO: put all the font prefs in an enum?
    public enum FONT_PREFS
    {
        KEY_FONT_FAMILY(StyleConstants.FontFamily.toString(), DEFAULT_FONT.getFamily());

        String keyStr = "";
        String defaultStr = "";

        FONT_PREFS (String keyStr, String defaultStr)
        {
            this.keyStr = keyStr;
            this.defaultStr = defaultStr;
        }
    }

    public enum ConfigKeys {
        KEY_FIRST_CHANNEL(Constants.KEY_FIRST_CHANNEL, DEFAULT_FIRST_CHANNEL),
        KEY_FIRST_SERVER(Constants.KEY_FIRST_SERVER, DEFAULT_FIRST_SERVER),
        KEY_FIRST_PORT(Constants.KEY_FIRST_PORT, DEFAULT_FIRST_PORT),
        KEY_AUTH_TYPE(Constants.KEY_AUTH_TYPE, DEFAULT_AUTH_TYPE),
        KEY_PASSWORD_REMEMBER(Constants.KEY_PASSWORD_REMEMBER, DEFAULT_PASSWORD_REMEMBER),
        KEY_PASSWORD(Constants.KEY_PASSWORD, DEFAULT_PASSWORD),
        KEY_USE_TLS(Constants.KEY_USE_TLS, DEFAULT_USE_TLS),
        KEY_PROXY_TYPE(Constants.KEY_PROXY_TYPE, DEFAULT_PROXY_TYPE),
        KEY_PROXY_HOST(Constants.KEY_PROXY_HOST, DEFAULT_PROXY_HOST),
        KEY_PROXY_PORT(Constants.KEY_PROXY_PORT, DEFAULT_PROXY_PORT),
        KEY_NICK_NAME(Constants.KEY_NICK_NAME, DEFAULT_NICK_NAME),
        KEY_REAL_NAME(Constants.KEY_REAL_NAME, DEFAULT_REAL_NAME),
        KEY_SHOW_TAB_ICON(Constants.KEY_SHOW_TAB_ICON, DEFAULT_SHOW_TAB_ICON),
        KEY_TIME_STAMPS(Constants.KEY_TIME_STAMPS, DEFAULT_TIME_STAMPS),
        KEY_TIME_STAMP_FORMAT(Constants.KEY_TIME_STAMP_FORMAT, DEFAULT_TIME_STAMP_FORMAT),
        KEY_NICK_FORMAT(Constants.KEY_NICK_FORMAT, DEFAULT_NICK_FORMAT),
        KEY_LAF_NAME(Constants.KEY_LAF_NAME, DEFAULT_LAF_NAME),
        KEY_EVENT_TICKER_ACTIVE(Constants.KEY_EVENT_TICKER_ACTIVE, DEFAULT_EVENT_TICKER_ACTIVE),
        KEY_USERS_LIST_ACTIVE(Constants.KEY_USERS_LIST_ACTIVE, DEFAULT_USERS_LIST_ACTIVE),
        KEY_CLICKABLE_LINKS_ENABLED(Constants.KEY_CLICKABLE_LINKS_ENABLED, DEFAULT_CLICKABLE_LINKS_ENABLED),
        KEY_EVENT_TICKER_JOINS_QUITS(Constants.KEY_EVENT_TICKER_JOINS_QUITS, DEFAULT_EVENT_TICKER_JOINS_QUITS),
        KEY_MAIN_WINDOW_JOINS_QUITS(Constants.KEY_MAIN_WINDOW_JOINS_QUITS, DEFAULT_MAIN_WINDOW_JOINS_QUITS),
        KEY_LOG_CHANNEL_ACTIVITY(Constants.KEY_LOG_CHANNEL_ACTIVITY, DEFAULT_LOG_CHANNEL_ACTIVITY),
        KEY_LOG_SERVER_ACTIVITY(Constants.KEY_LOG_SERVER_ACTIVITY, DEFAULT_LOG_SERVER_ACTIVITY),
        KEY_LIMIT_CHANNEL_LINES(Constants.KEY_LIMIT_CHANNEL_LINES, DEFAULT_LIMIT_CHANNEL_LINES),
        KEY_LIMIT_CHANNEL_LINES_COUNT(Constants.KEY_LIMIT_CHANNEL_LINES_COUNT, DEFAULT_LIMIT_CHANNEL_LINES_COUNT),
        KEY_LIMIT_SERVER_LINES(Constants.KEY_LIMIT_SERVER_LINES, DEFAULT_LIMIT_SERVER_LINES),
        KEY_LIMIT_SERVER_LINES_COUNT(Constants.KEY_LIMIT_SERVER_LINES_COUNT, DEFAULT_LIMIT_SERVER_LINES_COUNT),
        KEY_AUTO_CONNECT_FAVOURITES(Constants.KEY_AUTO_CONNECT_FAVOURITES, DEFAULT_AUTO_CONNECT_FAVOURITES),
        KEY_LOG_CLIENT_TEXT(Constants.KEY_LOG_CLIENT_TEXT, DEFAULT_LOG_CLIENT_TEXT),
        KEY_FONT_FAMILY(Constants.KEY_FONT_FAMILY, DEFAULT_STYLE.getFamily()),
        KEY_FONT_BOLD(Constants.KEY_FONT_BOLD, DEFAULT_STYLE.isBold()),
        KEY_FONT_ITALIC(Constants.KEY_FONT_ITALIC, DEFAULT_STYLE.isItalic()),
        KEY_FONT_UNDERLINE(Constants.KEY_FONT_UNDERLINE, DEFAULT_STYLE.isUnderline()),
        KEY_FONT_SIZE(Constants.KEY_FONT_SIZE, DEFAULT_STYLE.getSize()),
        KEY_FONT_FOREGROUND(Constants.KEY_FONT_FOREGROUND, DEFAULT_FONT_FOREGROUND),
        KEY_FONT_BACKGROUND(Constants.KEY_FONT_BACKGROUND, DEFAULT_FONT_BACKGROUND),
        KEY_WINDOW_X(Constants.KEY_WINDOW_X, DEFAULT_WINDOW_X),
        KEY_WINDOW_Y(Constants.KEY_WINDOW_Y, DEFAULT_WINDOW_Y),
        KEY_WINDOW_WIDTH(Constants.KEY_WINDOW_WIDTH, DEFAULT_WINDOW_WIDTH),
        KEY_WINDOW_HEIGHT(Constants.KEY_WINDOW_HEIGHT, DEFAULT_WINDOW_HEIGHT),
        KEY_DEFAULT_PROFILE_NAME(Constants.KEY_DEFAULT_PROFILE_NAME, DEFAULT_PROFILE_NAME),
        KEY_EVENT_TICKER_DELAY(Constants.KEY_EVENT_TICKER_DELAY, DEFAULT_EVENT_TICKER_DELAY);

        private final String keyName;
        private final Object defaultValue; // Assuming all default values are of Object type

        ConfigKeys (String keyName, Object defaultValue)
        {
            this.keyName = keyName;
            this.defaultValue = defaultValue;
        }

        @Override
        public String toString ()
        {
            return keyName;
        }

        public String getKeyName ()
        {
            return keyName;
        }

        public Object getDefaultValue ()
        {
            return defaultValue;
        }

        /**
         * Get default value based on the key string
         * @param keyName
         * @return
         */
        public static Object getDefault (String keyName)
        {
            for (ConfigKeys key : values())
            {
                if (key.getKeyName().equals(keyName))
                {
                    return key.getDefaultValue();
                }
            }
            return null; // Or handle default behavior if the key doesn't exist
        }
    }

    /**
     * Used to initialize some values that may throw exceptions.
     */
    public static void init ()
    {
        URVersionLabel.setVersion();

        try
        {
            new Object().getClass();
            BACKEND_CLASS = Class.forName(BACKEND_CLASS_FULLNAME);
        } catch (Exception e)
        {
            LOGGER.warn("BACKEND_CLASS failed to load! Defaulting to default implementation");
            BACKEND_CLASS = Connection.class;
        }
    }
}
