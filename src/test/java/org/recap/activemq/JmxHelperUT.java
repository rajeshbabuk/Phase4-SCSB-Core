package org.recap.activemq;

import org.apache.activemq.broker.jmx.DestinationViewMBean;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.recap.BaseTestCase;
import org.recap.BaseTestCaseUT;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.util.ReflectionTestUtils;

import javax.management.MBeanServerConnection;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;


public class JmxHelperUT extends BaseTestCaseUT {


    private static final Logger logger = LoggerFactory.getLogger(JmxHelper.class);

    @InjectMocks
    JmxHelper jmxHelper;

    String serviceUrl = "https://serviceUrl.com";

    @Before
    public void setup(){
        ReflectionTestUtils.setField(jmxHelper,"serviceUrl",serviceUrl);
    }

    @Test
    public void testGetBeanForQueueName() {
        MBeanServerConnection connection = Mockito.mock(MBeanServerConnection.class);
        ReflectionTestUtils.setField(jmxHelper,"connection",connection);
        DestinationViewMBean DestinationViewMBean = null;
        DestinationViewMBean = jmxHelper.getBeanForQueueName("test");
        assertNotNull(DestinationViewMBean);
    }

    @Test
    public void testGetBeanForQueueNameNull() {
        MBeanServerConnection connection = Mockito.mock(MBeanServerConnection.class);
        ReflectionTestUtils.setField(jmxHelper,"connection",connection);
        DestinationViewMBean DestinationViewMBean = null;
        DestinationViewMBean = jmxHelper.getBeanForQueueName(null);
        assertNotNull(DestinationViewMBean);
    }

    @Test
    public void testGetConnection() {
        MBeanServerConnection connection= null;
        try {
            connection = jmxHelper.getConnection();
        } catch (Exception e) {
            logger.info("Exception" + e);
        }
        assertNull(connection);
    }

}
