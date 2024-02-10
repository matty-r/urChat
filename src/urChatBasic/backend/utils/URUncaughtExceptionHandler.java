package urChatBasic.backend.utils;

import urChatBasic.base.Constants;

public class URUncaughtExceptionHandler implements Thread.UncaughtExceptionHandler {

    @Override
    public void uncaughtException(Thread t, Throwable e) {
        Constants.LOGGER.error( "Uncaught exception: " + e.getStackTrace(), e);
    }
}