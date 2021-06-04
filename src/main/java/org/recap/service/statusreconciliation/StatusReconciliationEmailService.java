package org.recap.service.statusreconciliation;

import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;
import org.apache.camel.ProducerTemplate;
import org.recap.PropertyKeyConstants;
import org.recap.ScsbConstants;
import org.recap.camel.EmailPayLoad;
import org.recap.util.PropertyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

/**
 * Created by hemalathas on 31/5/17.
 */
@Service
@Scope("prototype")
@Slf4j
public class StatusReconciliationEmailService {

    @Autowired
    private ProducerTemplate producerTemplate;

    @Autowired
    PropertyUtil propertyUtil;


    /**
     * Sets the email payload for the status reconciliation.
     *
     * @param exchange the exchange
     */
    public void processInput(Exchange exchange) {
        String fileLocation = (String) exchange.getIn().getHeader(ScsbConstants.CAMEL_AWS_KEY);
        producerTemplate.sendBodyAndHeader(ScsbConstants.EMAIL_Q, getEmailPayLoad(fileLocation,exchange), ScsbConstants.EMAIL_BODY_FOR, ScsbConstants.STATUS_RECONCILIATION);
    }

    private EmailPayLoad getEmailPayLoad(String fileLocation,Exchange exchange) {
        EmailPayLoad emailPayLoad = new EmailPayLoad();
        emailPayLoad.setTo(propertyUtil.getPropertyByImsLocationAndKey((String) exchange.getIn().getHeader(ScsbConstants.IMS_LOCATION), PropertyKeyConstants.EMAIL_STATUS_RECONCILIATION_TO));
        emailPayLoad.setCc(propertyUtil.getPropertyByImsLocationAndKey((String) exchange.getIn().getHeader(ScsbConstants.IMS_LOCATION), PropertyKeyConstants.EMAIL_STATUS_RECONCILIATION_CC));
        log.info("Status Reconciliation : email sent to : {} and cc : {} ", emailPayLoad.getTo(), emailPayLoad.getCc());
        emailPayLoad.setMessageDisplay("The \"Out\" Status Reconciliation report is available at the S3 location " + fileLocation);
        return emailPayLoad;
    }
}
