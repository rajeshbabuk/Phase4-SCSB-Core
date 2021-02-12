package org.recap.ils.model;

import org.junit.Test;
import org.recap.BaseTestCaseUT;

import static org.junit.Assert.assertTrue;

public class RecordTypeTypeUT extends BaseTestCaseUT {


    @Test
    public void testfromValue(){
        RecordTypeType.fromValue("Bibliographic");
        assertTrue(true);
    }
}
