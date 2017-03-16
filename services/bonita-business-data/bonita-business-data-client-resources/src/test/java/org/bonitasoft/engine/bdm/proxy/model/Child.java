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
package org.bonitasoft.engine.bdm.proxy.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.Date;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.bonitasoft.engine.bdm.Entity;

public class Child implements Entity {

    private static final long serialVersionUID = 3000056129946432830L;

    private String name;
    private Integer age;

    private Date oldDate;

    private LocalDate birthdate;

    private LocalDateTime localDateTime;

    private OffsetDateTime nextAppointment;

    public Child() {
    }

    public Child(String name, Integer age, Date oldDate, LocalDate birthdate, LocalDateTime localDateTime, OffsetDateTime nextAppointment) {
        this.name = name;
        this.age = age;
        this.oldDate = oldDate;
        this.birthdate = birthdate;
        this.localDateTime = localDateTime;
        this.nextAppointment = nextAppointment;
    }

    public Child(String name, Integer age, Date oldDate, LocalDate birthdate, LocalDateTime localDateTime) {
        this.name = name;
        this.age = age;
        this.oldDate = oldDate;
        this.birthdate = birthdate;
        this.localDateTime = localDateTime;
    }

    public Child(String name, Integer age) {
        this.name = name;
        this.age = age;
    }

    public Date getOldDate() {
        return oldDate;
    }

    public void setOldDate(Date oldDate) {
        this.oldDate = oldDate;
    }

    public LocalDate getBirthdate() {
        return birthdate;
    }

    public void setBirthdate(LocalDate birthdate) {
        this.birthdate = birthdate;
    }

    public LocalDateTime getLocalDateTime() {
        return localDateTime;
    }

    public void setLocalDateTime(LocalDateTime localDateTime) {
        this.localDateTime = localDateTime;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public OffsetDateTime getNextAppointment() {
        return nextAppointment;
    }

    public void setNextAppointment(OffsetDateTime nextAppointment) {
        this.nextAppointment = nextAppointment;
    }

    @Override
    public Long getPersistenceId() {
        return null;
    }

    @Override
    public Long getPersistenceVersion() {
        return null;
    }

    public String toJson() {
        return "{\"name\" : \"" + name + "\", \"age\" : " + age + ", \"oldDate\" : " + (oldDate != null ? oldDate.getTime() : null)
                + ", \"birthdate\" : " + (birthdate != null ? "\"" + birthdate.toString() + "\"" : null) + ", \"localDateTime\" : "
                + (localDateTime != null ? "\"" + localDateTime.toString() + "\"" : null) + ", \"nextAppointment\" : "
                + (nextAppointment != null ? "\""
                        // leave TimeZone offset as is, if any, as this is the Deserializer job to convert it to UTC:
                        + nextAppointment.toString() + "\"" : null)
                + " }";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;

        if (!(o instanceof Child))
            return false;

        Child child = (Child) o;

        return new EqualsBuilder()
                .append(getName(), child.getName())
                .append(getAge(), child.getAge())
                .append(getOldDate(), child.getOldDate())
                .append(getBirthdate(), child.getBirthdate())
                .append(getLocalDateTime(), child.getLocalDateTime())
                .append(getNextAppointment(), child.getNextAppointment())
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(getName())
                .append(getAge())
                .append(getOldDate())
                .append(getBirthdate())
                .append(getLocalDateTime())
                .append(getNextAppointment())
                .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("name", name)
                .append("age", age)
                .append("oldDate", oldDate)
                .append("birthdate", birthdate)
                .append("localDateTime", localDateTime)
                .append("nextAppointment", nextAppointment)
                .toString();
    }
}
