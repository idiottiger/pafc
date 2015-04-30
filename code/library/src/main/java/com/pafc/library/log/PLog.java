package com.pafc.library.log;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * <pre>
 * system Log replace class
 * 1. can config the console log visibility and the minimum level
 * 2. can add custom {@link com.pafc.library.log.PLog.Config}
 * </pre>
 */
public final class PLog {

    public static final int LEVEL_VERBOSE = Log.VERBOSE;
    public static final int LEVEL_DEBUG = Log.DEBUG;
    public static final int LEVEL_INFO = Log.INFO;
    public static final int LEVEL_WARN = Log.WARN;
    public static final int LEVEL_ERROR = Log.ERROR;

    private static List<Config> sLogConfigs = new ArrayList<>();
    private static Config sConsoleLogConfig = new Config.Builder().setLogLevel(LEVEL_VERBOSE).setLogVisible(true).setLogHandler(new Config.ILogHandler() {
        @Override
        public void onLog(int level, String tag, String message) {
            Log.println(level, tag, message);
        }
    }).build();

    static {
        sLogConfigs.add(sConsoleLogConfig);
    }

    public static void setConsoleLogVisible(boolean logVisible) {
        sConsoleLogConfig.isLogVisible = logVisible;
    }

    public static void setConsoleMinimumLogLevel(int logLevel) {
        if (logLevel >= LEVEL_VERBOSE && logLevel <= LEVEL_ERROR) {
            sConsoleLogConfig.minimumLogLevel = logLevel;
        }
    }

    public static void addLogConfig(Config config) {
        sLogConfigs.add(config);
    }

    public static void removeLogConfig(Config config) {
        sLogConfigs.remove(config);
    }

    public static class Config {

        public static interface ILogHandler {
            public void onLog(int level, String tag, String message);
        }

        int minimumLogLevel = LEVEL_VERBOSE;
        boolean isLogVisible = true;
        ILogHandler logHandler = null;

        public static class Builder {

            private Config mConfig;

            public Builder() {
                mConfig = new Config();
            }

            public Builder setLogLevel(int level) {
                if (level >= LEVEL_VERBOSE && level <= LEVEL_ERROR) {
                    mConfig.minimumLogLevel = level;
                }
                return this;
            }

            public Builder setLogVisible(boolean visible) {
                mConfig.isLogVisible = visible;
                return this;
            }

            public Builder setLogHandler(ILogHandler handler) {
                mConfig.logHandler = handler;
                return this;
            }

            public Config build() {
                return mConfig;
            }
        }

    }

    public static void v(String message) {
        log(LEVEL_VERBOSE, null, message, null);
    }

    public static void v(String tag, String message) {
        log(LEVEL_VERBOSE, tag, message, null);
    }

    public static void vF(String fmt, Object... args) {
        log(LEVEL_VERBOSE, null, String.format(fmt, args), null);
    }

    public static void vF(String tag, String fmt, Object... args) {
        log(LEVEL_VERBOSE, tag, String.format(fmt, args), null);
    }

    public static void d(String message) {
        log(LEVEL_DEBUG, null, message, null);
    }

    public static void d(String tag, String message) {
        log(LEVEL_DEBUG, tag, message, null);
    }

    public static void dF(String fmt, Object... args) {
        log(LEVEL_DEBUG, null, String.format(fmt, args), null);
    }

    public static void dF(String tag, String fmt, Object... args) {
        log(LEVEL_DEBUG, tag, String.format(fmt, args), null);
    }

    public static void i(String message) {
        log(LEVEL_INFO, null, message, null);
    }

    public static void i(String tag, String message) {
        log(LEVEL_INFO, tag, message, null);
    }

    public static void iF(String fmt, Object... args) {
        log(LEVEL_INFO, null, String.format(fmt, args), null);
    }

    public static void iF(String tag, String fmt, Object... args) {
        log(LEVEL_INFO, tag, String.format(fmt, args), null);
    }

    public static void w(String message) {
        log(LEVEL_WARN, null, message, null);
    }

    public static void w(String tag, String message) {
        log(LEVEL_WARN, tag, message, null);
    }

    public static void w(String tag, String message, Throwable throwable) {
        log(LEVEL_WARN, tag, message, throwable);
    }

    public static void wF(String fmt, Object... args) {
        log(LEVEL_WARN, null, String.format(fmt, args), null);
    }

    public static void wF(String tag, String fmt, Object... args) {
        log(LEVEL_WARN, tag, String.format(fmt, args), null);
    }

    public static void e(String message) {
        log(LEVEL_ERROR, null, message, null);
    }

    public static void e(String tag, String message) {
        log(LEVEL_ERROR, tag, message, null);
    }

    public static void e(String tag, String message, Throwable throwable) {
        log(LEVEL_ERROR, tag, message, throwable);
    }

    public static void eF(String fmt, Object... args) {
        log(LEVEL_ERROR, null, String.format(fmt, args), null);
    }

    public static void eF(String tag, String fmt, Object... args) {
        log(LEVEL_ERROR, tag, String.format(fmt, args), null);
    }


    private static void log(int level, String tag, String message, Throwable throwable) {
        final String newTag = tag == null ? createTag() : tag;
        final String newMessage = throwable == null ? message : (message + "\n" + Log.getStackTraceString(throwable));
        for (Config config : sLogConfigs) {
            if (config.isLogVisible && level >= config.minimumLogLevel && config.logHandler != null) {
                config.logHandler.onLog(level, newTag, newMessage);
            }
        }
    }

    private static String createTag() {
        final StackTraceElement[] stackTraceElements = new Throwable().getStackTrace();
        if (stackTraceElements != null) {
            StackTraceElement element = stackTraceElements[3];
            return element.getClassName() + " " + element.getMethodName() + ":" + element.getLineNumber();
        }
        return "";
    }


}
