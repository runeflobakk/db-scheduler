package com.github.kagkarlsson.scheduler;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.ThrowableProxy;
import ch.qos.logback.core.read.ListAppender;
import com.github.kagkarlsson.scheduler.logging.LogLevel;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.slf4j.LoggerFactory;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;

public class FailureLoggerTest {

    private static class TestException extends RuntimeException {}

    private final Logger logger = (Logger) LoggerFactory.getLogger(this.getClass());
    private final ListAppender<ILoggingEvent> appender = new ListAppender<>();

    {
        logger.setLevel(ch.qos.logback.classic.Level.ALL);
        appender.start();
    }

    @BeforeEach
    public void addAppender() {
        logger.addAppender(appender);
    }

    @AfterEach
    public void removeAppender() {
        logger.detachAppender(appender);
    }

    @ParameterizedTest
    @EnumSource(LogLevel.class)
    public void should_log_using_correct_log_level(LogLevel level) {
        FailureLogger failureLogger = new FailureLogger(logger, level, false);
        failureLogger.log("test {}", null, "test");

        ILoggingEvent logEvent = appender.list.get(0);
        assertThat(logEvent.getLevel().levelStr, is(level.name()));
        assertThat(logEvent.getFormattedMessage(), is("test test"));
    }

    @ParameterizedTest
    @EnumSource(LogLevel.class)
    public void should_log_stack_trace_if_configured(LogLevel level) {
        TestException cause = new TestException();

        FailureLogger failureLogger = new FailureLogger(logger, level, true);
        failureLogger.log("test {}", cause, "test");

        ILoggingEvent logEvent = appender.list.get(0);
        assertThat(logEvent.getLevel().levelStr, is(level.name()));
        assertThat(logEvent.getFormattedMessage(), is("test test"));

        // use Logback's implementation directly since the supertype doesn't have a method for returning the throwable
        ThrowableProxy throwableProxy = (ThrowableProxy) logEvent.getThrowableProxy();
        assertThat(throwableProxy, is(notNullValue()));
        assertThat(throwableProxy.getThrowable(), is(cause));
    }

}
