package urChatBasic.base;

import java.io.File;

import urChatBasic.backend.Connection;

public class Constants{

	public static final String RESOURCES_DIR = "Resources" + File.separator;
	public static String DIRECTORY_LOGS = "Logs" + File.separator;
	public static Class BACKEND_CLASS = Connection.class;

}
