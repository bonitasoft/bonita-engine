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
public class LookNFeelCreator implements Serializable {

    private static final long serialVersionUID = -1414989152963184543L;

    public enum LookNFeelField {
        CONTENT, IS_DEFAULT, TYPE;
    }

    private final Map<LookNFeelField, Serializable> fields;

    public LookNFeelCreator() {
        fields = new HashMap<LookNFeelField, Serializable>(3);
    }

    public LookNFeelCreator setContent(final byte[] content) {
        fields.put(LookNFeelField.CONTENT, content);
        return this;
    }

    public LookNFeelCreator setDefault(final boolean isDefault) {
        fields.put(LookNFeelField.IS_DEFAULT, isDefault);
        return this;
    }

    public LookNFeelCreator setType(final LookNFeelType type) {
        fields.put(LookNFeelField.TYPE, type);
        return this;
    }

    public Map<LookNFeelField, Serializable> getFields() {
        return fields;
    }

}
