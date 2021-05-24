package org.recap.controllerit;

import org.junit.Test;
import org.recap.ScsbCommonConstants;
import org.springframework.test.web.servlet.MvcResult;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class SubmitCollectionJobControllerIT extends BaseControllerUT{

    @Test
    public void teststartSubmitCollection() throws Exception{
        MvcResult mvcResult = this.mockMvc.perform(post("/submitCollectionJob/startSubmitCollection")
        ).andExpect(status().isOk())
                .andReturn();
        String result = mvcResult.getResponse().getContentAsString();
        assertNotNull(result);
        int status = mvcResult.getResponse().getStatus();
        assertTrue(status == 200);
        assertEquals(ScsbCommonConstants.SUCCESS,result);
    }
}
