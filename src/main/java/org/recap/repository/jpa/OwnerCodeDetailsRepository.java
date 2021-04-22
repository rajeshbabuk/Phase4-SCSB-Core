package org.recap.repository.jpa;


import org.recap.model.jpa.OwnerCodeEntity;

import java.util.List;


/**
 * The interface Owner code details repository.
 */
public interface OwnerCodeDetailsRepository extends BaseRepository<OwnerCodeEntity> {

    /**
     * Find by owner code owner code entity.
     *
     * @param ownerCode the owner code
     * @return the owner code entity
     */
    OwnerCodeEntity findByOwnerCode(String ownerCode);


    /**
     * Find by owner code owner code entity.
     *
     * @param ownerCode the owner code
     * @param imsLocationId the ImsLocation Id
     * @return the owner code entity
     */
    OwnerCodeEntity findByOwnerCodeAndImsLocationId(String ownerCode, Integer imsLocationId);

    /**
     * Find by owner code in list.
     *
     * @param ownerCodes the owner codes
     * @return the list
     */
    List<OwnerCodeEntity> findByOwnerCodeIn(List<String> ownerCodes);

}
