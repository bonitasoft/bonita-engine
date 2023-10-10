/**
 * Copyright (C) 2019 Bonitasoft S.A.
 * Bonitasoft, 32 rue Gustave Eiffel - 38000 Grenoble
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

import lombok.AllArgsConstructor;
import lombok.Data;
import org.bonitasoft.engine.bdm.Entity;

@Data
@AllArgsConstructor
public class Child implements Entity {

    private static final long serialVersionUID = 3000056129946432830L;

    private String name;
    private Integer age;
    private Date oldDate;
    private LocalDate birthdate;
    private LocalDateTime localDateTime;
    private OffsetDateTime nextAppointment;

    public Child() {
        // Empty constructor required for json serialization
    }

    public Child(String name, Integer age) {
        this.name = name;
        this.age = age;
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
        return "{\"name\" : \"" + name + "\", \"age\" : " + age + ", \"oldDate\" : "
                + (oldDate != null ? oldDate.getTime() : null)
                + ", \"birthdate\" : " + (birthdate != null ? "\"" + birthdate.toString() + "\"" : null)
                + ", \"localDateTime\" : "
                + (localDateTime != null ? "\"" + localDateTime.toString() + "\"" : null) + ", \"nextAppointment\" : "
                + (nextAppointment != null ? "\""
                        // leave TimeZone offset as is, if any, as this is the Deserializer job to convert it to UTC:
                        + nextAppointment.toString() + "\"" : null)
                + " }";
    }
}
