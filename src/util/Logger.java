package util;

import java.io.IOException;
import java.util.logging.Level;

public class Logger {
    private static java.util.logging.Logger globalLogger;
    private static java.util.Map<String, java.util.logging.Logger> loggerMap = new java.util.HashMap<>(10);
    
    private static final java.util.logging.ConsoleHandler cHandler;
    private static final java.util.logging.FileHandler fHandler;
    private static final java.util.logging.Formatter formatter;
    private static String dirPath;
    private static final Level defaultLevel = Level.WARNING;
    
    private Logger() { }
    
    static {
        cHandler = new java.util.logging.ConsoleHandler();
        java.io.File file = new java.io.File("res/logs");
        try {
            if(file.exists()) {
                Logger.dirPath = "res/logs";
            } else {
                Logger.dirPath = ".";
            }
            fHandler = new java.util.logging.FileHandler(Logger.dirPath + "/log.txt");
        } catch(IOException e) {
            throw new java.lang.IllegalStateException("Failed to create logging file handler");
        } 
        formatter = new java.util.logging.SimpleFormatter() {
            private static final String format = "[%1$tF %1$tT.%1$tL | %3$-10s | %4$-7s]: %5$s %6$s%n";
            
            @Override
            public synchronized String format(java.util.logging.LogRecord lr) {
                Throwable t;
                StringBuilder exceptionStr = new StringBuilder("");
                if((t = lr.getThrown()) != null) {
                    String NEWLINE = System.lineSeparator();
                    exceptionStr.append(NEWLINE);
                    exceptionStr.append(t.toString());
                    exceptionStr.append(NEWLINE);
                    for(java.lang.StackTraceElement s : t.getStackTrace()) {
                        exceptionStr.append("\t" + s.toString() + NEWLINE);
                    }
                    if((t = t.getCause()) != null) {
                        exceptionStr.append("Caused by: " + t.toString() + NEWLINE);
                        exceptionStr.append("\t... " + t.getStackTrace().length + " more" + NEWLINE);
                    }
                }    
                return String.format(format, new java.util.Date(lr.getMillis()), 
                                             lr.getSourceClassName(), 
                                             lr.getLoggerName(),
                                             lr.getLevel().getLocalizedName(),
                                             lr.getMessage(),
                                             exceptionStr.toString());
            }
            
        };
        Logger.cHandler.setFormatter(Logger.formatter);
        Logger.fHandler.setFormatter(Logger.formatter);
    }
            
    //Loads settings via static initializer and creates a global logger.
    public static void loadSettings() { 
        if(Logger.globalLogger == null) {
            Logger.globalLogger = Logger.getLogger("GLOBAL");
        }       
    }
    
    public static java.util.logging.Logger getLogger(String className) {
        if(loggerMap.get(className) == null) {
            java.util.logging.Logger logger = java.util.logging.Logger.getLogger(className);
            logger.setUseParentHandlers(false);
            logger.addHandler(Logger.fHandler);
            logger.setLevel(Logger.defaultLevel);
            Logger.loggerMap.put(className, logger);
        }
        return loggerMap.get(className);
    }
    
    public static String getLoggingPath() {
        return Logger.dirPath;
    }
}
