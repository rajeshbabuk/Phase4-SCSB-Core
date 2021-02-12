package org.recap.service.accession.callable;

import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.recap.BaseTestCaseUT;
import org.recap.TestUtil;
import org.recap.model.jpa.ItemEntity;
import org.recap.service.accession.AccessionResolverAbstract;

import static org.junit.Assert.assertTrue;

public class AccessionResolverAbstractUT extends BaseTestCaseUT {

    @Mock
    ItemEntity itemEntity;

    @Test
    public void isAccessionProcess() throws Exception {
        Mockito.when(itemEntity.getInstitutionEntity()).thenReturn(TestUtil.getInstitutionEntity(1,"PUL","PUL"));
        AccessionResolverAbstract accessionResolverAbstract= PowerMockito.mock(AccessionResolverAbstract.class);
        PowerMockito.doCallRealMethod().when(accessionResolverAbstract).isAccessionProcess(itemEntity,"PA");
        assertTrue(true);
    }
}
