/*******************************************************************************
 * Copyright (C) 2014 Bonitasoft S.A.
 * Bonitasoft is a trademark of Bonitasoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * Bonitasoft, 32 rue Gustave Eiffel 38000 Grenoble
 * or Bonitasoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package org.bonitasoft.engine.bdm.model.field;

import java.util.Date;

/**
 * @author Matthieu Chaffotte
 */
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
