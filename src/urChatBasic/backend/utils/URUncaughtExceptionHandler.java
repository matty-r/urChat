package urChatBasic.backend.utils;

import java.util.logging.Level;
import urChatBasic.base.Constants;

public class URUncaughtExceptionHandler implements Thread.UncaughtExceptionHandler {

    @Override
    public void uncaughtException(Thread t, Throwable e) {
        Constants.LOGGER.log(Level.SEVERE, "Uncaught exception in thread: " + t.getName(), e);
    }
}