package org.recap.service;

import org.apache.camel.ProducerTemplate;
import org.recap.PropertyKeyConstants;
import org.recap.ScsbConstants;
import org.recap.camel.EmailPayLoad;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Created by sudhishk on 19/1/17.
 */
@Service
public class EmailService {

    @Value("${" + PropertyKeyConstants.EMAIL_DELETED_RECORDS_TO + "}")
    private String deletedRecordsMailTo;

    @Autowired
    private ProducerTemplate producer;

    /**
     *  Send email method for deleted records reporting.
     *
     * @param messageDisplay
     * @param patronBarcode
     * @param toInstitution
     * @param subject
     */
    public void sendEmail(String messageDisplay, String patronBarcode, String toInstitution, String subject) {
        EmailPayLoad emailPayLoad = new EmailPayLoad();
        emailPayLoad.setTo(emailIdTo(toInstitution));
        emailPayLoad.setMessageDisplay(messageDisplay);
        emailPayLoad.setPatronBarcode(patronBarcode);
        emailPayLoad.setSubject(subject);
        producer.sendBodyAndHeader(ScsbConstants.EMAIL_Q, emailPayLoad, ScsbConstants.EMAIL_BODY_FOR, ScsbConstants.DELETED_MAIL_QUEUE);
    }

    /**
     * @param institution
     * @return
     */
    private String emailIdTo(String institution) {
        if(institution.equalsIgnoreCase(ScsbConstants.DELETED_MAIL_TO)){
            return deletedRecordsMailTo;
        }
        return null;
    }
}
