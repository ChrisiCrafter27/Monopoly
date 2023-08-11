package monopol.utils;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.logging.Level;

public abstract class CustomLogger {
    private final Logger LOGGER;

    protected CustomLogger(String logPath) {
        LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
        LOGGER.setLevel(Level.ALL);
        LOGGER.setUseParentHandlers(false);
        FileHandler fileHandler;
        try {
            fileHandler = new FileHandler(logPath);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        fileHandler.setFormatter(new Formatter() {
            @Override
            public String format(LogRecord record) {
                String result = "";

                //Date
                SimpleDateFormat df = new SimpleDateFormat("[dd-MM-yyyy HH:mm:ss]");
                Date date = new Date(record.getMillis());
                result += df.format(date);

                //Type
                if(record.getLevel().intValue() <= Level.FINE.intValue()) {
                    result = result + " [FINE] " + this.formatMessage(record);
                } else if (record.getLevel().intValue() == Level.CONFIG.intValue()) {
                    result = result + " [CONF] " + this.formatMessage(record);
                } else if(record.getLevel().intValue() == Level.INFO.intValue()) {
                    result = result + " [INFO] " + this.formatMessage(record);
                } else if(record.getLevel().intValue() == Level.WARNING.intValue()) {
                    result = result + " [WARN] " + this.formatMessage(record);
                } else if (record.getLevel().intValue() >= Level.SEVERE.intValue()) {
                    result = result + " [FAIL] " + this.formatMessage(record);
                }

                result += "\r\n";
                return result;
            }
        });
        LOGGER.addHandler(fileHandler);
    }

    public Logger getLogger() {
        return LOGGER;
    }

    public abstract CustomLogger getInstance();
}
