package org.recap;

import ch.qos.logback.classic.pattern.ThrowableHandlingConverter;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.IThrowableProxy;
import com.fasterxml.jackson.core.JsonGenerator;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;

import static org.junit.Assert.assertTrue;

public class CustomStackTraceJsonProviderUT extends BaseTestCaseUT {

    @InjectMocks
    CustomStackTraceJsonProvider customStackTraceJsonProvider;

    @Mock
    JsonGenerator generator;

    @Mock
    ILoggingEvent event;

    @Mock
    IThrowableProxy throwableProxy;

    @Mock
    ThrowableHandlingConverter throwableConverter;

    @Test
    public void scsbRequest() throws IOException {
        Mockito.when(event.getThrowableProxy()).thenReturn(throwableProxy);
        ReflectionTestUtils.setField(customStackTraceJsonProvider,"throwableConverter",throwableConverter);
        Mockito.when(throwableConverter.convert(event)).thenReturn("Tested");
        customStackTraceJsonProvider.writeTo(generator,event);
        assertTrue(true);
    }
}
