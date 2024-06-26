package monopol.common.log;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.logging.Level;

public abstract class CustomLogger {
    private final Logger logger;

    protected CustomLogger(String logPath) {
        logger = Logger.getAnonymousLogger();
        logger.setLevel(Level.ALL);
        logger.setUseParentHandlers(false);
        FileHandler fileHandler;
        try {
            File file = new File("logs");
            if(!file.exists()) {
                file.mkdirs();
                file.createNewFile();
            }
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
                    result += " [FINE] " + formatMessage(record);
                } else if (record.getLevel().intValue() == Level.CONFIG.intValue()) {
                    result += " [CONF] " + formatMessage(record);
                } else if(record.getLevel().intValue() == Level.INFO.intValue()) {
                    result += " [INFO] " + formatMessage(record);
                } else if(record.getLevel().intValue() == Level.WARNING.intValue()) {
                    result += " [WARN] " + formatMessage(record);
                } else if (record.getLevel().intValue() >= Level.SEVERE.intValue()) {
                    result += " [FAIL] " + formatMessage(record);
                }

                result += "\r\n";
                return result;
            }
        });
        logger.addHandler(fileHandler);
    }

    public Logger get() {
        return logger;
    }

    public abstract CustomLogger getInstance();
}
