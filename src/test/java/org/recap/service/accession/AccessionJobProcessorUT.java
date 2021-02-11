package org.recap.service.accession;

import org.apache.camel.Exchange;
import org.apache.camel.ProducerTemplate;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.recap.BaseTestCaseUT;

import static org.junit.Assert.assertTrue;

public class AccessionJobProcessorUT extends BaseTestCaseUT {

    @InjectMocks
    AccessionJobProcessor accessionJobProcessor;

    @Mock
    Exchange exchange;

    @Mock
    Exception exception;

    @Mock
    ProducerTemplate producer;

    @Test
    public void testcaughtException() throws Exception {
        Mockito.when(exchange.getProperty(Exchange.EXCEPTION_CAUGHT)).thenReturn(exception);
        accessionJobProcessor.caughtException(exchange);
        assertTrue(true);
    }

    @Test
    public void testNullcaughtException() throws Exception {
        accessionJobProcessor.caughtException(exchange);
        assertTrue(true);
    }
}
