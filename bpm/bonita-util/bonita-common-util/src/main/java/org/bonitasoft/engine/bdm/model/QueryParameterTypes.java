/**
 * Copyright (C) 2018 BonitaSoft S.A.
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
package org.bonitasoft.engine.bdm.model;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.Date;
import java.util.stream.Stream;

public enum QueryParameterTypes {

    STRING(String.class.getCanonicalName(), String.class),
    INTEGER(Integer.class.getCanonicalName(), Integer.class), 
    DOUBLE(Double.class.getCanonicalName(), Double.class),
    LONG(Long.class.getCanonicalName(), Long.class),
    FLOAT(Float.class.getCanonicalName(), Float.class),
    DATE(Date.class.getCanonicalName(), Date.class),
    BOOLEAN(Boolean.class.getCanonicalName(),Boolean.class),
    LOCALDATETIME(LocalDateTime.class.getCanonicalName(),LocalDateTime.class),
    LOCALDATE(LocalDate.class.getCanonicalName(),LocalDate.class),
    OFFSETDATETIME(OffsetDateTime.class.getCanonicalName(),OffsetDateTime.class),
    STRING_ARRAY(String[].class.getCanonicalName(),String[].class),
    INT_ARRAY(Integer[].class.getCanonicalName(),Integer[].class),
    LONG_ARRAY(Long[].class.getCanonicalName(),Long[].class),
    DOUBLE_ARRAY(Double[].class.getCanonicalName(), Double[].class),
    FLOAT_ARRAY(Float[].class.getCanonicalName(), Float[].class);
    
    private Class<?> clazz;
    private String name;

    private QueryParameterTypes(String name, final Class<?> clazz) {
        this.name = name;
        this.clazz = clazz;
    }

    public String getName() {
        return name;
    }

    public Class<?> getClazz() {
        return clazz;
    }

    /**
     * Check if the given value is a supported type as a query parameter
     */
    public static boolean contains(Class<? extends Serializable> clazz) {
        return Stream.of(QueryParameterTypes.values())
                .map(QueryParameterTypes::getClazz)
                .anyMatch(clazz::equals);
    }

}
