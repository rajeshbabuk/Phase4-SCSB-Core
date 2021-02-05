package org.recap.controller;

import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.recap.BaseTestCaseUT;
import org.recap.service.EncryptEmailAddressService;

import static org.junit.Assert.assertNotNull;

public class EncryptEmailAddressUT extends BaseTestCaseUT {

    @InjectMocks
    EncryptEmailAddress encryptEmailAddress;

    @Mock
    EncryptEmailAddressService encryptEmailAddressService;

    @Test
    public void startEncryptEmailAddress(){
        Mockito.when(encryptEmailAddressService.encryptEmailAddress()).thenReturn("test");
        String result = encryptEmailAddress.startEncryptEmailAddress();
        assertNotNull(result);
    }
}
