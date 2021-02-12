package org.recap.camel.statusreconciliation;

import org.junit.Test;
import org.recap.BaseTestCaseUT;
import org.recap.model.csv.StatusReconciliationCSVRecord;
import org.recap.model.csv.StatusReconciliationErrorCSVRecord;

import java.util.Date;

import static org.junit.Assert.assertNotNull;

/**
 * Created by hemalathas on 13/7/17.
 */
public class StatusReconciliationCSVRecordUT extends BaseTestCaseUT {

    @Test
    public void testStatusReconciliationCSVRecord(){
        StatusReconciliationCSVRecord statusReconciliationCSVRecord = new StatusReconciliationCSVRecord();
        StatusReconciliationErrorCSVRecord statusReconciliationErrorCSVRecord = new StatusReconciliationErrorCSVRecord();
        statusReconciliationCSVRecord.setBarcode("33245645454584");
        statusReconciliationCSVRecord.setRequestAvailability("Yes");
        statusReconciliationCSVRecord.setRequestId("1235");
        statusReconciliationCSVRecord.setStatusInScsb("OUT");
        statusReconciliationCSVRecord.setStatusInLas("IN");
        statusReconciliationCSVRecord.setDateTime(new Date().toString());
        statusReconciliationCSVRecord.setImsLocation("RECAP");
        statusReconciliationErrorCSVRecord.setBarcode("33245645454584");
        statusReconciliationErrorCSVRecord.setReasonForFailure("Barcode not found in LAS");
        statusReconciliationErrorCSVRecord.setInstitution("PUL");
        statusReconciliationErrorCSVRecord.setImsLocation("RECAP");
        assertNotNull(statusReconciliationCSVRecord.getBarcode());
        assertNotNull(statusReconciliationCSVRecord.getRequestAvailability());
        assertNotNull(statusReconciliationCSVRecord.getRequestId());
        assertNotNull(statusReconciliationCSVRecord.getStatusInLas());
        assertNotNull(statusReconciliationCSVRecord.getStatusInScsb());
        assertNotNull(statusReconciliationCSVRecord.getDateTime());
        assertNotNull(statusReconciliationCSVRecord.getImsLocation());
        assertNotNull(statusReconciliationErrorCSVRecord.getBarcode());
        assertNotNull(statusReconciliationErrorCSVRecord.getInstitution());
        assertNotNull(statusReconciliationErrorCSVRecord.getReasonForFailure());
        assertNotNull(statusReconciliationErrorCSVRecord.getImsLocation());
    }

}