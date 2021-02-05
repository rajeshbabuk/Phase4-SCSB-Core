package org.recap.camel.dailyreconciliation;


import org.apache.camel.Exchange;
import org.apache.camel.Header;
import org.apache.camel.Message;
import org.apache.camel.ProducerTemplate;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.recap.BaseTestCaseUT;
import org.recap.util.PropertyUtil;
import org.springframework.test.util.ReflectionTestUtils;


public class DailyReconciliationEmailServiceUT extends BaseTestCaseUT {

    @InjectMocks
    DailyReconciliationEmailService dailyReconciliationEmailService;

    @Mock
    ProducerTemplate producerTemplate;

    @Mock
    Exchange exchange;

    @Mock
    Message message;

    @Mock
    Header dataheader;

    @Mock
    PropertyUtil propertyUtil;

    @Before
    public  void setup(){
        ReflectionTestUtils.setField(dailyReconciliationEmailService,"fileLocation","fileLocation");
        ReflectionTestUtils.setField(dailyReconciliationEmailService,"imsLocationCode","RECAP");
    }

    @Test
    public void process() throws Exception {
        message.setHeader("CamelFileNameProduced",dataheader);
        exchange.setIn(message);
        Mockito.when(exchange.getIn()).thenReturn(message);
        dailyReconciliationEmailService.process(exchange);
    }
}
