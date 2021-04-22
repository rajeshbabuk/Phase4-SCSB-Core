package org.recap.model.jpa;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.List;
import java.util.Objects;

/**
 * Created by rajeshbabuk on 18/10/16.
 */
@Entity
@Table(name = "OWNER_CODES_T", schema = "recap", catalog = "")
@AttributeOverride(name = "id", column = @Column(name = "OWNER_CODE_ID"))
@Getter
@Setter
public class OwnerCodeEntity extends OwnerCodeAbstractEntity  implements Comparable<OwnerCodeEntity> {

    @Column(name = "CIRC_DESK_LOCATION")
    private String pickupLocation;


    /*@ManyToMany(cascade = CascadeType.ALL)
    @JoinTable(name = "cross_partner_mapping_t", joinColumns = {
            @JoinColumn(name = "CUSTOMER_CODE_ID", referencedColumnName = "CUSTOMER_CODE_ID")},
            inverseJoinColumns = {
                    @JoinColumn(name = "DELIVERY_RESTRICTION_CROSS_PARTNER_ID", referencedColumnName = "DELIVERY_RESTRICTION_CROSS_PARTNER_ID")})
    private List<DeliveryRestrictionEntity> deliveryRestrictionEntityList;
*/
    @Override
    public int compareTo(OwnerCodeEntity ownerCodeEntity) {
        if (null != this.getDescription() && null !=  ownerCodeEntity && null != ownerCodeEntity.getDescription()) {
            return this.getDescription().compareTo(ownerCodeEntity.getDescription());
        }
        return 0;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object)
            return true;
        if (object == null || getClass() != object.getClass())
            return false;

        OwnerCodeEntity ownerCodeEntity = (OwnerCodeEntity) object;

        if (!Objects.equals(id, ownerCodeEntity.id))
            return false;
        if (getOwnerCode() != null ? !getOwnerCode().equals(ownerCodeEntity.getOwnerCode()) : ownerCodeEntity.getOwnerCode() != null)
            return false;
        if (getDescription() != null ? !getDescription().equals(ownerCodeEntity.getDescription()) : ownerCodeEntity.getDescription() != null)
            return false;
        return getInstitutionId() != null ? getInstitutionId().equals(ownerCodeEntity.getInstitutionId()) : ownerCodeEntity.getInstitutionId() == null;

    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (getOwnerCode() != null ? getOwnerCode().hashCode() : 0);
        result = 31 * result + (getDescription() != null ? getDescription().hashCode() : 0);
        result = 31 * result + (getInstitutionId() != null ? getInstitutionId().hashCode() : 0);
        return result;
    }
}
