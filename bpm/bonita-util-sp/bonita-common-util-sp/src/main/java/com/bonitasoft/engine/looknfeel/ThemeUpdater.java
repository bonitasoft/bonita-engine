/*******************************************************************************
 * Copyright (C) 2013 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.looknfeel;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Celine Souchet
 */
public class ThemeUpdater implements Serializable {

    private static final long serialVersionUID = 728214104237982027L;

    public enum ThemeField {
        CONTENT, TYPE;
    }

    private final Map<ThemeField, Serializable> fields;

    public ThemeUpdater() {
        fields = new HashMap<ThemeField, Serializable>(3);
    }

    public ThemeUpdater setContent(final byte[] content) {
        fields.put(ThemeField.CONTENT, content);
        return this;
    }

    public ThemeUpdater setType(final ThemeType type) {
        fields.put(ThemeField.TYPE, type);
        return this;
    }

    public Map<ThemeField, Serializable> getFields() {
        return fields;
    }

}
