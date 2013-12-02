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
public class LookNFeelUpdater implements Serializable {

    private static final long serialVersionUID = 728214104237982027L;

    public enum LookNFeelField {
        CONTENT, TYPE;
    }

    private final Map<LookNFeelField, Serializable> fields;

    public LookNFeelUpdater() {
        fields = new HashMap<LookNFeelField, Serializable>(3);
    }

    public LookNFeelUpdater setContent(final byte[] content) {
        fields.put(LookNFeelField.CONTENT, content);
        return this;
    }

    public LookNFeelUpdater setType(final LookNFeelType type) {
        fields.put(LookNFeelField.TYPE, type);
        return this;
    }

    public Map<LookNFeelField, Serializable> getFields() {
        return fields;
    }

}
