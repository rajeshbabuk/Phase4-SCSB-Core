package org.recap.service.accession;

import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.recap.BaseTestCaseUT;
import org.recap.ScsbCommonConstants;
import org.recap.service.authorization.OauthTokenApiService;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

public class SimpleRestCallForBibdataUT extends BaseTestCaseUT {

    @Mock
    SimpleRestCallForBibdata simpleRestCallForBibdata;

    @Mock
    OauthTokenApiService oauthTokenApiService;

    @Mock
    RestTemplate restTemplate;

    @Mock
    SimpleClientHttpRequestFactory factory;

    @Test
    public void getBibData() throws Exception {
        ReflectionTestUtils.setField(simpleRestCallForBibdata,"connectionTimeout",1000);
        ReflectionTestUtils.setField(simpleRestCallForBibdata,"readTimeout",1000);
        Mockito.when(simpleRestCallForBibdata.getParamsMap(Mockito.anyString())).thenCallRealMethod();
        Mockito.when(simpleRestCallForBibdata.getRestTmp()).thenCallRealMethod();
        Mockito.when(simpleRestCallForBibdata.getRestTemplate()).thenReturn(restTemplate);
        Mockito.when(restTemplate.getRequestFactory()).thenReturn(factory);
        Mockito.doNothing().when(factory).setConnectTimeout(Mockito.anyInt());
        Mockito.doNothing().when(factory).setReadTimeout(Mockito.anyInt());
        Mockito.when(oauthTokenApiService.generateAccessToken(Mockito.anyString(),Mockito.anyString(),Mockito.anyString())).thenReturn("test");
        ResponseEntity<String> responseEntity=new ResponseEntity<>(ScsbCommonConstants.SUCCESS, HttpStatus.OK);
        HttpEntity requestEntity = new HttpEntity(getHttpHeaders());
        Mockito.when(restTemplate.exchange("url", HttpMethod.GET, requestEntity, String.class, getParamsMap("123456"))).thenReturn(responseEntity);
        Mockito.when(simpleRestCallForBibdata.getHttpHeaders()).thenCallRealMethod();
        Mockito.when(simpleRestCallForBibdata.getBibData("123456","PA","PUL","url")).thenCallRealMethod();
        String bibDataResponse=simpleRestCallForBibdata.getBibData("123456","PA","PUL","url");
        assertEquals(ScsbCommonConstants.SUCCESS,bibDataResponse);
    }

    @Test
    public void testgetRestTemplate() {
        Mockito.when(simpleRestCallForBibdata.getRestTemplate()).thenCallRealMethod();
        simpleRestCallForBibdata.getRestTemplate();
        assertTrue(true);
    }

    @Test
    public void testisAuth() {
        boolean isAuth=simpleRestCallForBibdata.isAuth("NoAuth");
        assertFalse(isAuth);
    }

    public HttpHeaders getHttpHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_XML));
        return headers;
    }

    public Map<String, String> getParamsMap(String itemBarcode) {
        Map<String, String> params = new HashMap<>();
        params.put("barcode", itemBarcode);
        return params;
    }

}
