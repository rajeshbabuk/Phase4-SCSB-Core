package org.recap.service.accession;

import org.apache.camel.Exchange;
import org.apache.camel.ProducerTemplate;
import org.recap.PropertyKeyConstants;
import org.recap.ScsbConstants;
import org.recap.camel.EmailPayLoad;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

@Service
@Scope("prototype")
public class AccessionJobProcessor {

    private static final Logger logger = LoggerFactory.getLogger(AccessionJobProcessor.class);

    @Autowired
    private ProducerTemplate producer;

    @Value("${" + PropertyKeyConstants.EMAIL_ACCESSION_JOB_EXCEPTION_TO + "}")
    private String emailTo;

    @Value("${" + PropertyKeyConstants.EMAIL_ACCESSION_JOB_EXCEPTION_CC + "}")
    private String emailCc;

    public void caughtException(Exchange exchange) {
        logger.info("inside caught exception..........");
        Exception exception = (Exception) exchange.getProperty(Exchange.EXCEPTION_CAUGHT);
        if (exception != null) {
            producer.sendBodyAndHeader(ScsbConstants.EMAIL_Q, getEmailPayLoadForException(exception, exception.getMessage()), ScsbConstants.EMAIL_FOR, ScsbConstants.ACCESSION_JOB_FAILURE);
        }
    }

    private EmailPayLoad getEmailPayLoadForException(Exception exception, String exceptionMessage) {
        EmailPayLoad emailPayLoad = new EmailPayLoad();
        emailPayLoad.setSubject(ScsbConstants.ACCESSION_JOB_FAILURE);
        emailPayLoad.setTo(emailTo);
        emailPayLoad.setCc(emailCc);
        emailPayLoad.setMessage("An exception has occurred in Ongoing Accession process. \n Exception : " + exception + "\n Exception Message : " + exceptionMessage);
        return emailPayLoad;
    }
}
