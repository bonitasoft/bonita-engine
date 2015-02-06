
package com.company.model;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Version;


/**
 *
 */
@javax.persistence.Entity(name = "Phone")
@Table(name = "PHONE")
@NamedQueries({
        @NamedQuery(name = "Phone.findByNumber", query = "SELECT p\nFROM Phone p\nWHERE p.number= :number\nORDER BY p.persistenceId"),
        @NamedQuery(name = "Phone.find", query = "SELECT p\nFROM Phone p\nORDER BY p.persistenceId")
})
public class Phone
        implements com.bonitasoft.engine.bdm.Entity
{

    @Id
    @GeneratedValue
    private Long persistenceId;
    @Version
    private Long persistenceVersion;
    @Column(name = "NUMBER", nullable = true, length = 255)
    private String number;

    public Phone() {
    }

    public void setPersistenceId(Long persistenceId) {
        this.persistenceId = persistenceId;
    }

    public Long getPersistenceId() {
        return persistenceId;
    }

    public void setPersistenceVersion(Long persistenceVersion) {
        this.persistenceVersion = persistenceVersion;
    }

    public Long getPersistenceVersion() {
        return persistenceVersion;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getNumber() {
        return number;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass()!= obj.getClass()) {
            return false;
        }
        Phone other = ((Phone) obj);
        if (persistenceId == null) {
            if (other.persistenceId!= null) {
                return false;
            }
        } else {
            if (!persistenceId.equals(other.persistenceId)) {
                return false;
            }
        }
        if (persistenceVersion == null) {
            if (other.persistenceVersion!= null) {
                return false;
            }
        } else {
            if (!persistenceVersion.equals(other.persistenceVersion)) {
                return false;
            }
        }
        if (number == null) {
            if (other.number!= null) {
                return false;
            }
        } else {
            if (!number.equals(other.number)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        int persistenceIdCode = 0;
        if (persistenceId!= null) {
            persistenceIdCode = persistenceId.hashCode();
        }
        result = ((prime*result)+ persistenceIdCode);
        int persistenceVersionCode = 0;
        if (persistenceVersion!= null) {
            persistenceVersionCode = persistenceVersion.hashCode();
        }
        result = ((prime*result)+ persistenceVersionCode);
        int numberCode = 0;
        if (number!= null) {
            numberCode = number.hashCode();
        }
        result = ((prime*result)+ numberCode);
        return result;
    }

}
