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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.ElementCollection;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.OrderColumn;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Version;

import org.apache.commons.beanutils.converters.DateTimeConverter;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.bonitasoft.engine.bdm.Entity;
import org.bonitasoft.engine.business.data.generator.DateConverter;
import org.bonitasoft.engine.business.data.generator.OffsetDateTimeConverter;

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

    @Convert(converter = DateConverter.class)
    @Column(name = "LOCALDATE", nullable = true, length = 10)
    private LocalDate aLocalDate;

    @Convert(converter = DateTimeConverter.class)
    @Column(name = "LOCALDATETIME", nullable = true, length = 30)
    private LocalDateTime aLocalDateTime;

    @Convert(converter = OffsetDateTimeConverter.class)
    @Column(name = "OFFSETDATETIME", nullable = true, length = 40)
    private OffsetDateTime anOffsetDateTime;

    public EntitySerializerPojo() {
    }

    public OffsetDateTime getAnOffsetDateTime() {
        return anOffsetDateTime;
    }

    public void setAnOffsetDateTime(OffsetDateTime anOffsetDateTime) {
        this.anOffsetDateTime = anOffsetDateTime;
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

    public LocalDate getALocalDate() {
        return aLocalDate;
    }

    public void setALocalDate(LocalDate localDate) {
        this.aLocalDate = localDate;
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

    public LocalDateTime getALocalDateTime() {
        return aLocalDateTime;
    }

    public void setALocalDateTime(LocalDateTime aLocalDateTime) {
        this.aLocalDateTime = aLocalDateTime;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;

        if (!(o instanceof EntitySerializerPojo))
            return false;

        EntitySerializerPojo that = (EntitySerializerPojo) o;

        return new EqualsBuilder()
                .append(getPersistenceId(), that.getPersistenceId())
                .append(getPersistenceVersion(), that.getPersistenceVersion())
                .append(aString, that.aString)
                .append(aBoolean, that.aBoolean)
                .append(aDate, that.aDate)
                .append(aDouble, that.aDouble)
                .append(aFloat, that.aFloat)
                .append(aInteger, that.aInteger)
                .append(aLong, that.aLong)
                .append(aText, that.aText)
                .append(getManyLong(), that.getManyLong())
                .append(getManyString(), that.getManyString())
                .append(aLocalDate, that.aLocalDate)
                .append(aLocalDateTime, that.aLocalDateTime)
                .append(getAnOffsetDateTime(), that.getAnOffsetDateTime())
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(getPersistenceId())
                .append(getPersistenceVersion())
                .append(aString)
                .append(aBoolean)
                .append(aDate)
                .append(aDouble)
                .append(aFloat)
                .append(aInteger)
                .append(aLong)
                .append(aText)
                .append(getManyLong())
                .append(getManyString())
                .append(aLocalDate)
                .append(aLocalDateTime)
                .append(getAnOffsetDateTime())
                .toHashCode();
    }
}
