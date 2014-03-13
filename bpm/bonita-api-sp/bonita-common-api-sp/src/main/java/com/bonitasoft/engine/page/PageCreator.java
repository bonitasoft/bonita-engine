/*******************************************************************************
 * Copyright (C) 2013 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.page;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Laurent Leseigneur
 */
public class PageCreator implements Serializable {

    public enum PageField {
        NAME, DESCRIPTION, DISPLAYNAME;
    }

    private static final long serialVersionUID = 8174091386958635983L;

    private final Map<PageField, Serializable> fields;

    public PageCreator(final String name) {
        fields = new HashMap<PageField, Serializable>(2);
        fields.put(PageField.NAME, name);
    }

    public PageCreator setDisplayName(final String displayName) {
        fields.put(PageField.DISPLAYNAME, displayName);
        return this;
    }

    public PageCreator setDescription(final String description) {
        fields.put(PageField.DESCRIPTION, description);
        return this;
    }

    public Map<PageField, Serializable> getFields() {
        return fields;
    }

}
