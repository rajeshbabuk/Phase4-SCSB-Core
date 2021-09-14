package org.recap.service.statusreconciliation;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.recap.PropertyKeyConstants;
import org.recap.ScsbCommonConstants;
import org.recap.TestUtil;
import org.recap.model.csv.StatusReconciliationCSVRecord;
import org.recap.model.csv.StatusReconciliationErrorCSVRecord;
import org.recap.model.gfa.ScsbLasItemStatusCheckModel;
import org.recap.model.jpa.ItemEntity;
import org.recap.model.jpa.ItemStatusEntity;
import org.recap.model.jpa.RequestItemEntity;
import org.recap.model.jpa.RequestStatusEntity;
import org.recap.repository.jpa.ItemChangeLogDetailsRepository;
import org.recap.repository.jpa.ItemDetailsRepository;
import org.recap.repository.jpa.ItemStatusDetailsRepository;
import org.recap.repository.jpa.RequestItemDetailsRepository;
import org.recap.repository.jpa.RequestItemStatusDetailsRepository;
import org.recap.service.SolrDocIndexService;
import org.recap.util.CommonUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertNotNull;

@RunWith(MockitoJUnitRunner.class)
@TestPropertySource("classpath:application.properties")
public class StatusReconciliationServiceUT {

    @InjectMocks
    StatusReconciliationService statusReconciliationService;

    @Mock
    StatusReconciliationService mockStatusReconciliationService;

    @Mock
    CommonUtil commonUtil;

    @Mock
    ItemChangeLogDetailsRepository itemChangeLogDetailsRepository;

    @Value("${" + PropertyKeyConstants.SCSB_CIRC_URL + "}")
    String scsbCircUrl;

    @Mock
    ScsbLasItemStatusCheckModel mockScsbLasItemStatusCheckModel;

    @Mock
    RequestItemStatusDetailsRepository requestItemStatusDetailsRepository;

    @Mock
    RequestStatusEntity requestStatusEntity;

    @Mock
    RequestItemDetailsRepository requestItemDetailsRepository;

    @Mock
    ItemStatusDetailsRepository itemStatusDetailsRepository;

    @Mock
    ItemStatusEntity itemStatusEntity;

    @Mock
    RequestItemEntity requestItemEntity;

    @Mock
    ItemDetailsRepository itemDetailsRepository;

    @Mock
    SolrDocIndexService solrDocIndexService;

    @Mock
    RequestStatusEntity byRequestStatusCode;


    @Test
    public void itemStatusComparisonCancelRequest() throws Exception {
        List<List<ItemEntity>> itemEntityChunkList=new ArrayList<>();
        ItemEntity itemEntity=new ItemEntity();
        itemEntity.setBarcode("123456");
        itemEntity.setImsLocationEntity(TestUtil.getImsLocationEntity(3,"RECAP","RECAP_LAS"));
        List<ItemEntity> itemEntityList=new ArrayList<>();
        itemEntityList.add(itemEntity);
        itemEntityChunkList.add(itemEntityList);
        List<StatusReconciliationErrorCSVRecord> statusReconciliationErrorCSVRecordList=new ArrayList<>();
        Mockito.when(commonUtil.getScsbItemStatusModelListByItemEntities(Mockito.anyList())).thenCallRealMethod();
        List<ScsbLasItemStatusCheckModel> gfaItemStatusCheckResponseItems=new ArrayList<>();
        gfaItemStatusCheckResponseItems.add(mockScsbLasItemStatusCheckModel);
        ReflectionTestUtils.setField(mockStatusReconciliationService,"itemChangeLogDetailsRepository",itemChangeLogDetailsRepository);
        ReflectionTestUtils.setField(mockStatusReconciliationService,"commonUtil",commonUtil);
        ReflectionTestUtils.setField(mockStatusReconciliationService,"scsbCircUrl","scsbCircUrl");
        ReflectionTestUtils.setField(mockStatusReconciliationService,"scsbUrl","scsbUrl");
        ReflectionTestUtils.setField(mockStatusReconciliationService,"requestItemStatusDetailsRepository",requestItemStatusDetailsRepository);
        ReflectionTestUtils.setField(mockStatusReconciliationService,"requestItemDetailsRepository",requestItemDetailsRepository);
        ReflectionTestUtils.setField(mockStatusReconciliationService,"itemStatusDetailsRepository",itemStatusDetailsRepository);
        Mockito.when(mockStatusReconciliationService.itemStatusComparison(itemEntityChunkList,statusReconciliationErrorCSVRecordList, 0)).thenCallRealMethod();
        List<RequestStatusEntity> requestStatusEntityList=new ArrayList<>();
        requestStatusEntityList.add(requestStatusEntity);
        List<RequestItemEntity> requestItemEntityList=new ArrayList<>();
        requestItemEntityList.add(requestItemEntity);
        List<StatusReconciliationCSVRecord> statusReconciliationCSVRecordList=mockStatusReconciliationService.itemStatusComparison(itemEntityChunkList,statusReconciliationErrorCSVRecordList,0);
        assertNotNull(statusReconciliationCSVRecordList);
    }

    @Test
    public void getStatusReconciliationCSVRecordIsUnknownCode() throws Exception {
        Mockito.when(mockStatusReconciliationService.getStatusReconciliationCSVRecord("123456",ScsbCommonConstants.PRINCETON,ScsbCommonConstants.PRINCETON,"IN","1","IN","updatedDateTime","requestedDateTime",itemStatusEntity,"RECAP",true,true,false)).thenCallRealMethod();
        StatusReconciliationCSVRecord statusReconciliationCSVRecord =mockStatusReconciliationService.getStatusReconciliationCSVRecord("123456",ScsbCommonConstants.PRINCETON,ScsbCommonConstants.PRINCETON,"IN","1","IN","updatedDateTime","requestedDateTime",itemStatusEntity,"RECAP",true,true,false);
        assertNotNull(statusReconciliationCSVRecord);
    }

    @Test
    public void getStatusReconciliationCSVRecordIsRefileCapNotExceeded() throws Exception {
        Mockito.when(mockStatusReconciliationService.getStatusReconciliationCSVRecord("123456",ScsbCommonConstants.PRINCETON,ScsbCommonConstants.PRINCETON,"IN","1","IN","updatedDateTime","requestedDateTime",itemStatusEntity,"RECAP",false,true,true)).thenCallRealMethod();
        StatusReconciliationCSVRecord statusReconciliationCSVRecord =mockStatusReconciliationService.getStatusReconciliationCSVRecord("123456",ScsbCommonConstants.PRINCETON,ScsbCommonConstants.PRINCETON,"IN","1","IN","updatedDateTime","requestedDateTime",itemStatusEntity,"RECAP",false,true,true);
        assertNotNull(statusReconciliationCSVRecord);
    }

    @Test
    public void getStatusReconciliationCSVRecord() throws Exception {
        Mockito.when(mockStatusReconciliationService.getStatusReconciliationCSVRecord("123456",ScsbCommonConstants.PRINCETON,ScsbCommonConstants.PRINCETON,"IN","1","IN","updatedDateTime","requestedDateTime",itemStatusEntity,"RECAP",false,false,false)).thenCallRealMethod();
        StatusReconciliationCSVRecord statusReconciliationCSVRecord =mockStatusReconciliationService.getStatusReconciliationCSVRecord("123456",ScsbCommonConstants.PRINCETON,ScsbCommonConstants.PRINCETON,"IN","1","IN","updatedDateTime","requestedDateTime",itemStatusEntity,"RECAP",false,false,false);
        assertNotNull(statusReconciliationCSVRecord);
    }

    @Test
    public void itemStatusComparison() throws Exception {
        List<List<ItemEntity>> itemEntityChunkList=new ArrayList<>();
        ItemEntity itemEntity=new ItemEntity();
        itemEntity.setBarcode("123456");
        itemEntity.setImsLocationEntity(TestUtil.getImsLocationEntity(3,"RECAP","RECAP_LAS"));
        List<ItemEntity> itemEntityList=new ArrayList<>();
        itemEntityList.add(itemEntity);
        itemEntityChunkList.add(itemEntityList);
        List<StatusReconciliationErrorCSVRecord> statusReconciliationErrorCSVRecordList=new ArrayList<>();
        Mockito.when(commonUtil.getScsbItemStatusModelListByItemEntities(Mockito.anyList())).thenCallRealMethod();
        List<ScsbLasItemStatusCheckModel> gfaItemStatusCheckResponseItems=new ArrayList<>();
        gfaItemStatusCheckResponseItems.add(mockScsbLasItemStatusCheckModel);
        ReflectionTestUtils.setField(mockStatusReconciliationService,"itemChangeLogDetailsRepository",itemChangeLogDetailsRepository);
        ReflectionTestUtils.setField(mockStatusReconciliationService,"commonUtil",commonUtil);
        ReflectionTestUtils.setField(mockStatusReconciliationService,"scsbCircUrl","scsbCircUrl");
        ReflectionTestUtils.setField(mockStatusReconciliationService,"scsbUrl","scsbUrl");
        ReflectionTestUtils.setField(mockStatusReconciliationService,"requestItemStatusDetailsRepository",requestItemStatusDetailsRepository);
        ReflectionTestUtils.setField(mockStatusReconciliationService,"requestItemDetailsRepository",requestItemDetailsRepository);
        ReflectionTestUtils.setField(mockStatusReconciliationService,"itemStatusDetailsRepository",itemStatusDetailsRepository);
        Mockito.when(mockStatusReconciliationService.itemStatusComparison(itemEntityChunkList,statusReconciliationErrorCSVRecordList,0)).thenCallRealMethod();
        List<RequestStatusEntity> requestStatusEntityList=new ArrayList<>();
        requestStatusEntityList.add(requestStatusEntity);
        List<RequestItemEntity> requestItemEntityList=new ArrayList<>();
        requestItemEntityList.add(requestItemEntity);
        List<StatusReconciliationCSVRecord> statusReconciliationCSVRecordList=mockStatusReconciliationService.itemStatusComparison(itemEntityChunkList,statusReconciliationErrorCSVRecordList,0);
        assertNotNull(statusReconciliationCSVRecordList);
    }

    @Test
    public void itemStatusComparisonSolrCancelled() throws Exception {
        List<List<ItemEntity>> itemEntityChunkList=new ArrayList<>();
        ItemEntity itemEntity=new ItemEntity();
        itemEntity.setBarcode("123456");
        itemEntity.setImsLocationEntity(TestUtil.getImsLocationEntity(3,"RECAP","RECAP_LAS"));
        List<ItemEntity> itemEntityList=new ArrayList<>();
        itemEntityList.add(itemEntity);
        itemEntityChunkList.add(itemEntityList);
        List<StatusReconciliationErrorCSVRecord> statusReconciliationErrorCSVRecordList=new ArrayList<>();
        Mockito.when(commonUtil.getScsbItemStatusModelListByItemEntities(Mockito.anyList())).thenCallRealMethod();
        List<ScsbLasItemStatusCheckModel> gfaItemStatusCheckResponseItems=new ArrayList<>();
        gfaItemStatusCheckResponseItems.add(mockScsbLasItemStatusCheckModel);
        ReflectionTestUtils.setField(mockStatusReconciliationService,"itemChangeLogDetailsRepository",itemChangeLogDetailsRepository);
        ReflectionTestUtils.setField(mockStatusReconciliationService,"commonUtil",commonUtil);
        ReflectionTestUtils.setField(mockStatusReconciliationService,"scsbCircUrl","scsbCircUrl");
        ReflectionTestUtils.setField(mockStatusReconciliationService,"scsbUrl","scsbUrl");
        ReflectionTestUtils.setField(mockStatusReconciliationService,"requestItemStatusDetailsRepository",requestItemStatusDetailsRepository);
        ReflectionTestUtils.setField(mockStatusReconciliationService,"requestItemDetailsRepository",requestItemDetailsRepository);
        ReflectionTestUtils.setField(mockStatusReconciliationService,"itemStatusDetailsRepository",itemStatusDetailsRepository);
        ReflectionTestUtils.setField(mockStatusReconciliationService,"itemDetailsRepository",itemDetailsRepository);
        ReflectionTestUtils.setField(mockStatusReconciliationService,"solrDocIndexService",solrDocIndexService);
        Mockito.when(mockStatusReconciliationService.itemStatusComparison(itemEntityChunkList,statusReconciliationErrorCSVRecordList,0)).thenCallRealMethod();
        List<RequestStatusEntity> requestStatusEntityList=new ArrayList<>();
        requestStatusEntityList.add(requestStatusEntity);
        List<StatusReconciliationCSVRecord> statusReconciliationCSVRecordList=mockStatusReconciliationService.itemStatusComparison(itemEntityChunkList,statusReconciliationErrorCSVRecordList,0);
        assertNotNull(statusReconciliationCSVRecordList);
    }

    @Test
    public void itemStatusComparisonSolr() throws Exception {
        List<List<ItemEntity>> itemEntityChunkList=new ArrayList<>();
        ItemEntity itemEntity=new ItemEntity();
        itemEntity.setBarcode("123456");
        itemEntity.setImsLocationEntity(TestUtil.getImsLocationEntity(3,"RECAP","RECAP_LAS"));
        List<ItemEntity> itemEntityList=new ArrayList<>();
        itemEntityList.add(itemEntity);
        itemEntityChunkList.add(itemEntityList);
        List<StatusReconciliationErrorCSVRecord> statusReconciliationErrorCSVRecordList=new ArrayList<>();
        Mockito.when(commonUtil.getScsbItemStatusModelListByItemEntities(Mockito.anyList())).thenCallRealMethod();
        List<ScsbLasItemStatusCheckModel> gfaItemStatusCheckResponseItems=new ArrayList<>();
        gfaItemStatusCheckResponseItems.add(mockScsbLasItemStatusCheckModel);
        ReflectionTestUtils.setField(mockStatusReconciliationService,"itemChangeLogDetailsRepository",itemChangeLogDetailsRepository);
        ReflectionTestUtils.setField(mockStatusReconciliationService,"scsbCircUrl","scsbCircUrl");
        ReflectionTestUtils.setField(mockStatusReconciliationService,"scsbUrl","scsbUrl");
        ReflectionTestUtils.setField(mockStatusReconciliationService,"commonUtil",commonUtil);
        ReflectionTestUtils.setField(mockStatusReconciliationService,"requestItemStatusDetailsRepository",requestItemStatusDetailsRepository);
        ReflectionTestUtils.setField(mockStatusReconciliationService,"requestItemDetailsRepository",requestItemDetailsRepository);
        ReflectionTestUtils.setField(mockStatusReconciliationService,"itemStatusDetailsRepository",itemStatusDetailsRepository);
        Mockito.when(mockStatusReconciliationService.itemStatusComparison(itemEntityChunkList,statusReconciliationErrorCSVRecordList,0)).thenCallRealMethod();
        List<RequestStatusEntity> requestStatusEntityList=new ArrayList<>();
        requestStatusEntityList.add(requestStatusEntity);
        List<RequestItemEntity> requestItemEntityList=new ArrayList<>();
        requestItemEntityList.add(requestItemEntity);
        List<StatusReconciliationCSVRecord> statusReconciliationCSVRecordList=mockStatusReconciliationService.itemStatusComparison(itemEntityChunkList,statusReconciliationErrorCSVRecordList, 0);
        assertNotNull(statusReconciliationCSVRecordList);
    }


}
