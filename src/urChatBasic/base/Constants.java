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
