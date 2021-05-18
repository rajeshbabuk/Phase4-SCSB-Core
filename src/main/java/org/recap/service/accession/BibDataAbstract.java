package org.recap.service.accession;

import org.recap.PropertyKeyConstants;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public abstract class BibDataAbstract implements BibDataForAccessionInterface {

    @Value("${" + PropertyKeyConstants.BIBDATA_API_CONNECTION_TIMEOUT + "}")
    Integer connectionTimeout;

    @Value("${" + PropertyKeyConstants.BIBDATA_API_READ_TIMEOUT + "}")
    Integer readTimeout;

    public abstract boolean isAuth(String auth);

}
