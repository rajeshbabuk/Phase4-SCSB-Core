package org.recap.controller;

import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.recap.BaseTestCaseUT;
import org.recap.RecapConstants;
import org.recap.service.deletedrecords.DeletedRecordsService;
import org.springframework.http.ResponseEntity;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Created by hemalathas on 13/7/17.
 */
public class ReportDeletedRecordsControllerUT extends BaseTestCaseUT {

    @InjectMocks
    ReportDeletedRecordsController reportDeletedRecordsController;

    @Mock
    DeletedRecordsService deletedRecordsService;

    @Test
    public void deletedRecordsSuccess(){
        Mockito.when(deletedRecordsService.deletedRecords()).thenReturn(true);
        ResponseEntity responseEntity = reportDeletedRecordsController.deletedRecords();
        assertNotNull(responseEntity);
        assertEquals(RecapConstants.DELETED_RECORDS_SUCCESS_MSG, responseEntity.getBody());
    }

    @Test
    public void deletedRecordsFailure(){
        Mockito.when(deletedRecordsService.deletedRecords()).thenReturn(false);
        ResponseEntity responseEntity = reportDeletedRecordsController.deletedRecords();
        assertNotNull(responseEntity);
        assertEquals(RecapConstants.DELETED_RECORDS_FAILURE_MSG, responseEntity.getBody());
    }

}
