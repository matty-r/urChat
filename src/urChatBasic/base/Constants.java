package urChatBasic.base;

import java.io.File;

import urChatBasic.backend.Connection;

public class Constants{

	public static final String RESOURCES_DIR = "Resources" + File.separator;
	public static String directoryLogs = "Logs" + File.separator;
	public static final Class BACKEND_CLASS = Connection.class;

}
