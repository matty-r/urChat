package urChatBasic.base;

import java.io.File;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

import urChatBasic.backend.Connection;

/**
 * Used to store constants that are the same and do not change often. These are things used commonly across the [front,back]end
 * @author goofybud16
 *
 */
public class Constants{

	public static final String RESOURCES_DIR = "Resources" + File.separator;
	public static String DIRECTORY_LOGS = "Logs" + File.separator;
	public static Class BACKEND_CLASS;
	public static String BACKEND_CLASS_FULLNAME = "urChatBasic.backend.Connection";
	private static Handler LOGGER_TO_FILE;
	public static Logger LOGGER = Logger.getLogger("Main");
	public static String LOGFILE_NAME = "Errors.log";
	
	//Key Strings that are used when saving settings
	public static final String KEY_FIRST_CHANNEL = "first channel name";
	public static final String KEY_FIRST_SERVER = "first server name";
	public static final String KEY_FIRST_PORT = "first server port";
	public static final String KEY_NICK_NAME = "nick name";
	public static final String KEY_REAL_NAME = "real name";
	public static final String KEY_TIME_STAMPS = "show time stamps";
	public static final String KEY_EVENT_TICKER_ACTIVE = "show event ticker";
	public static final String KEY_USERS_LIST_ACTIVE = "show users list";
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
	public static final String KEY_FAVOURITES_NODE = "favourites";
	public static final String KEY_WINDOW_X = "window position x";
	public static final String KEY_WINDOW_Y = "window position y";
	public static final String KEY_WINDOW_WIDTH = "window position width";
	public static final String KEY_WINDOW_HEIGHT = "window position height";
	
	//Setting defaults
	public static final String DEFAULT_FIRST_CHANNEL = "##java";
	public static final String DEFAULT_FIRST_SERVER = "irc.freenode.net";
	public static final String DEFAULT_FIRST_PORT = "6667";
	public static final String DEFAULT_NICK_NAME = "urChatClient";
	public static final String DEFAULT_REAL_NAME = "urChatClient";
	public static final Boolean DEFAULT_TIME_STAMPS = true;
	public static final Boolean DEFAULT_EVENT_TICKER_ACTIVE = true;
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
	public static final int DEFAULT_EVENT_TICKER_DELAY = 20;
	public static final int DEFAULT_WINDOW_X = 0;
	public static final int DEFAULT_WINDOW_Y = 0;
	public static final int DEFAULT_WINDOW_WIDTH = 640;
	public static final int DEFAULT_WINDOW_HEIGHT = 480;
	
	
	/**
	 * Used to initialize some values that may throw exceptions.
	 * @author goofybud16
	 */
	public static void init(){
		try{
			File logDir = new File(DIRECTORY_LOGS);
			if(!logDir.exists()){
				logDir.mkdir();
			}
			File logFile = new File(Constants.DIRECTORY_LOGS, LOGFILE_NAME);
			if(!logFile.exists()){
				logFile.createNewFile();
			}
			LOGGER_TO_FILE = new FileHandler(logFile.getAbsolutePath());
			LOGGER.addHandler(LOGGER_TO_FILE);
		} catch(Exception e){
			LOGGER.log(Level.WARNING, "Failed to create LOGGER_TO_FILE: " + e.getLocalizedMessage());
		}
		try{
		BACKEND_CLASS = new Object().getClass().forName(BACKEND_CLASS_FULLNAME);
		} catch (Exception e)
		{
			LOGGER.log(Level.WARNING, "BACKEND_CLASS failed to load! Defaulting to default implementation");
			BACKEND_CLASS = Connection.class;
		}
	}
}
