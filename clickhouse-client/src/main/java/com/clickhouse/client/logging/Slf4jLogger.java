package com.clickhouse.client.logging;

import com.clickhouse.client.ClickHouseChecker;

/**
 * Adaptor for slf4j logger.
 */
public class Slf4jLogger implements Logger {
    private final org.slf4j.Logger logger;

    /**
     * Default constructor.
     *
     * @param logger non-null SLF4J logger
     */
    public Slf4jLogger(org.slf4j.Logger logger) {
        this.logger = ClickHouseChecker.nonNull(logger, "logger");
    }

    @Override
    public void debug(Runnable function) {
        if (function != null && logger.isDebugEnabled()) {
            function.run();
        }
    }

    @Override
    public void debug(Object format, Object... arguments) {
        if (logger.isDebugEnabled()) {
            LogMessage msg = LogMessage.of(format, arguments);
            if (msg.hasThrowable()) {
                logger.debug(msg.getMessage(), msg.getThrowable());
            } else {
                logger.debug(msg.getMessage());
            }
        }
    }

    @Override
    public void debug(Object message, Throwable t) {
        if (logger.isDebugEnabled()) {
            logger.debug(String.valueOf(message), t);
        }
    }

    @Override
    public void error(Runnable function) {
        if (function != null && logger.isErrorEnabled()) {
            function.run();
        }
    }

    @Override
    public void error(Object format, Object... arguments) {
        if (logger.isErrorEnabled()) {
            LogMessage msg = LogMessage.of(format, arguments);
            if (msg.hasThrowable()) {
                logger.error(msg.getMessage(), msg.getThrowable());
            } else {
                logger.error(msg.getMessage());
            }
        }
    }

    @Override
    public void error(Object message, Throwable t) {
        if (logger.isErrorEnabled()) {
            logger.error(String.valueOf(message), t);
        }
    }

    @Override
    public void info(Runnable function) {
        if (function != null && logger.isInfoEnabled()) {
            function.run();
        }
    }

    @Override
    public void info(Object format, Object... arguments) {
        if (logger.isInfoEnabled()) {
            LogMessage msg = LogMessage.of(format, arguments);
            if (msg.hasThrowable()) {
                logger.info(msg.getMessage(), msg.getThrowable());
            } else {
                logger.info(msg.getMessage());
            }
        }
    }

    @Override
    public void info(Object message, Throwable t) {
        if (logger.isInfoEnabled()) {
            logger.info(String.valueOf(message), t);
        }
    }

    @Override
    public void trace(Runnable function) {
        if (function != null && logger.isTraceEnabled()) {
            function.run();
        }
    }

    @Override
    public void trace(Object format, Object... arguments) {
        if (logger.isTraceEnabled()) {
            LogMessage msg = LogMessage.of(format, arguments);
            if (msg.hasThrowable()) {
                logger.trace(msg.getMessage(), msg.getThrowable());
            } else {
                logger.trace(msg.getMessage());
            }
        }
    }

    @Override
    public void trace(Object message, Throwable t) {
        if (logger.isTraceEnabled()) {
            logger.trace(String.valueOf(message), t);
        }
    }

    @Override
    public void warn(Runnable function) {
        if (function != null && logger.isWarnEnabled()) {
            function.run();
        }
    }

    @Override
    public void warn(Object format, Object... arguments) {
        if (logger.isWarnEnabled()) {
            LogMessage msg = LogMessage.of(format, arguments);
            if (msg.hasThrowable()) {
                logger.warn(msg.getMessage(), msg.getThrowable());
            } else {
                logger.warn(msg.getMessage());
            }
        }
    }

    @Override
    public void warn(Object message, Throwable t) {
        if (logger.isWarnEnabled()) {
            logger.warn(String.valueOf(message), t);
        }
    }

    @Override
    public Object unwrap() {
        return logger;
    }
}
