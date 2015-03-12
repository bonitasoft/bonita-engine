/*******************************************************************************
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.bdm.model.field;

import java.util.Date;

/**
 * @author Matthieu Chaffotte
 * @deprecated from version 7.0.0 on, use {@link org.bonitasoft.engine.bdm.model.field.FieldType} instead.
 */
@Deprecated
public enum FieldType {

    STRING(String.class),
    TEXT(String.class),
    INTEGER(Integer.class),
    DOUBLE(Double.class),
    LONG(Long.class),
    FLOAT(Float.class),
    DATE(Date.class),
    BOOLEAN(Boolean.class),
    BYTE(Byte.class),
    SHORT(Short.class),
    CHAR(Character.class);
    
    private Class<?> clazz;

    private FieldType(final Class<?> clazz) {
        this.clazz = clazz;
    }

    public Class<?> getClazz() {
        return clazz;
    }

}
