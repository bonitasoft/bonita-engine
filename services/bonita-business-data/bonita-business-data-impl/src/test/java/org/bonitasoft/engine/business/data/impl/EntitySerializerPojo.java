/**
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
 * This library is free software; you can redistribute it and/or modify it under the terms
 * of the GNU Lesser General Public License as published by the Free Software Foundation
 * version 2.1 of the License.
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License along with this
 * program; if not, write to the Free Software Foundation, Inc., 51 Franklin Street, Fifth
 * Floor, Boston, MA 02110-1301, USA.
 **/

package org.bonitasoft.engine.business.data.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.OrderColumn;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Version;

import org.bonitasoft.engine.bdm.Entity;

public class EntitySerializerPojo implements Entity {

    @Id
    @GeneratedValue
    private Long persistenceId;
    @Version
    private Long persistenceVersion;
    @Column(name = "ASTRING", nullable = true, length = 255)
    private String aString;
    @Column(name = "ABOOLEAN", nullable = true)
    private Boolean aBoolean;
    @Column(name = "ADATE", nullable = true)
    @Temporal(TemporalType.TIMESTAMP)
    private Date aDate;
    @Column(name = "ADOUBLE", nullable = true)
    private Double aDouble;
    @Column(name = "AFLOAT", nullable = true)
    private Float aFloat;
    @Column(name = "AINTEGER", nullable = true)
    private Integer aInteger;
    @Column(name = "ALONG", nullable = true)
    private Long aLong;
    @Column(name = "ATEXT", nullable = true)
    @Lob
    private String aText;
    @ElementCollection(fetch = FetchType.EAGER)
    @OrderColumn
    @Column(name = "MANYLONG", nullable = true)
    private List<Long> manyLong = new ArrayList<Long>(10);
    @ElementCollection(fetch = FetchType.EAGER)
    @OrderColumn
    @Column(name = "MANYSTRING", nullable = true, length = 255)
    private List<String> manyString = new ArrayList<String>(10);

    public EntitySerializerPojo() {
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

    public void setAString(String aString) {
        this.aString = aString;
    }

    public String getAString() {
        return aString;
    }

    public void setABoolean(Boolean aBoolean) {
        this.aBoolean = aBoolean;
    }

    public Boolean isABoolean() {
        return aBoolean;
    }

    public void setADate(Date aDate) {
        this.aDate = aDate;
    }

    public Date getADate() {
        return aDate;
    }

    public void setADouble(Double aDouble) {
        this.aDouble = aDouble;
    }

    public Double getADouble() {
        return aDouble;
    }

    public void setAFloat(Float aFloat) {
        this.aFloat = aFloat;
    }

    public Float getAFloat() {
        return aFloat;
    }

    public void setAInteger(Integer aInteger) {
        this.aInteger = aInteger;
    }

    public Integer getAInteger() {
        return aInteger;
    }

    public void setALong(Long aLong) {
        this.aLong = aLong;
    }

    public Long getALong() {
        return aLong;
    }

    public void setAText(String aText) {
        this.aText = aText;
    }

    public String getAText() {
        return aText;
    }

    public void setManyLong(List<Long> manyLong) {
        if (this.manyLong == null) {
            this.manyLong = manyLong;
        } else {
            this.manyLong.clear();
            this.manyLong.addAll(manyLong);
        }
    }

    public List<Long> getManyLong() {
        return manyLong;
    }

    public void addToManyLong(Long addTo) {
        List manyLong = getManyLong();
        manyLong.add(addTo);
    }

    public void removeFromManyLong(Long removeFrom) {
        List manyLong = getManyLong();
        manyLong.remove(removeFrom);
    }

    public void setManyString(List<String> manyString) {
        if (this.manyString == null) {
            this.manyString = manyString;
        } else {
            this.manyString.clear();
            this.manyString.addAll(manyString);
        }
    }

    public List<String> getManyString() {
        return manyString;
    }

    public void addToManyString(String addTo) {
        List manyString = getManyString();
        manyString.add(addTo);
    }

    public void removeFromManyString(String removeFrom) {
        List manyString = getManyString();
        manyString.remove(removeFrom);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        EntitySerializerPojo other = ((EntitySerializerPojo) obj);
        if (persistenceId == null) {
            if (other.persistenceId != null) {
                return false;
            }
        } else {
            if (!persistenceId.equals(other.persistenceId)) {
                return false;
            }
        }
        if (persistenceVersion == null) {
            if (other.persistenceVersion != null) {
                return false;
            }
        } else {
            if (!persistenceVersion.equals(other.persistenceVersion)) {
                return false;
            }
        }
        if (aString == null) {
            if (other.aString != null) {
                return false;
            }
        } else {
            if (!aString.equals(other.aString)) {
                return false;
            }
        }
        if (aBoolean == null) {
            if (other.aBoolean != null) {
                return false;
            }
        } else {
            if (!aBoolean.equals(other.aBoolean)) {
                return false;
            }
        }
        if (aDate == null) {
            if (other.aDate != null) {
                return false;
            }
        } else {
            if (!aDate.equals(other.aDate)) {
                return false;
            }
        }
        if (aDouble == null) {
            if (other.aDouble != null) {
                return false;
            }
        } else {
            if (!aDouble.equals(other.aDouble)) {
                return false;
            }
        }
        if (aFloat == null) {
            if (other.aFloat != null) {
                return false;
            }
        } else {
            if (!aFloat.equals(other.aFloat)) {
                return false;
            }
        }
        if (aInteger == null) {
            if (other.aInteger != null) {
                return false;
            }
        } else {
            if (!aInteger.equals(other.aInteger)) {
                return false;
            }
        }
        if (aLong == null) {
            if (other.aLong != null) {
                return false;
            }
        } else {
            if (!aLong.equals(other.aLong)) {
                return false;
            }
        }
        if (aText == null) {
            if (other.aText != null) {
                return false;
            }
        } else {
            if (!aText.equals(other.aText)) {
                return false;
            }
        }
        if (manyLong == null) {
            if (other.manyLong != null) {
                return false;
            }
        } else {
            if (!manyLong.equals(other.manyLong)) {
                return false;
            }
        }
        if (manyString == null) {
            if (other.manyString != null) {
                return false;
            }
        } else {
            if (!manyString.equals(other.manyString)) {
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
        if (persistenceId != null) {
            persistenceIdCode = persistenceId.hashCode();
        }
        result = ((prime * result) + persistenceIdCode);
        int persistenceVersionCode = 0;
        if (persistenceVersion != null) {
            persistenceVersionCode = persistenceVersion.hashCode();
        }
        result = ((prime * result) + persistenceVersionCode);
        int aStringCode = 0;
        if (aString != null) {
            aStringCode = aString.hashCode();
        }
        result = ((prime * result) + aStringCode);
        int aBooleanCode = 0;
        if (aBoolean != null) {
            aBooleanCode = aBoolean.hashCode();
        }
        result = ((prime * result) + aBooleanCode);
        int aDateCode = 0;
        if (aDate != null) {
            aDateCode = aDate.hashCode();
        }
        result = ((prime * result) + aDateCode);
        int aDoubleCode = 0;
        if (aDouble != null) {
            aDoubleCode = aDouble.hashCode();
        }
        result = ((prime * result) + aDoubleCode);
        int aFloatCode = 0;
        if (aFloat != null) {
            aFloatCode = aFloat.hashCode();
        }
        result = ((prime * result) + aFloatCode);
        int aIntegerCode = 0;
        if (aInteger != null) {
            aIntegerCode = aInteger.hashCode();
        }
        result = ((prime * result) + aIntegerCode);
        int aLongCode = 0;
        if (aLong != null) {
            aLongCode = aLong.hashCode();
        }
        result = ((prime * result) + aLongCode);
        int aTextCode = 0;
        if (aText != null) {
            aTextCode = aText.hashCode();
        }
        result = ((prime * result) + aTextCode);
        int manyLongCode = 0;
        if (manyLong != null) {
            manyLongCode = manyLong.hashCode();
        }
        result = ((prime * result) + manyLongCode);
        int manyStringCode = 0;
        if (manyString != null) {
            manyStringCode = manyString.hashCode();
        }
        result = ((prime * result) + manyStringCode);
        return result;
    }
}
