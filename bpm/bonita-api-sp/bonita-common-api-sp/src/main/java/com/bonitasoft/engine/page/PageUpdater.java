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
public class PageUpdater implements Serializable {

    private static final long serialVersionUID = 4295108162470507415L;

    public enum PageUpdateField {
        NAME, DISPLAY_NAME, DESCRIPTION, CONTENT_NAME;
    }

    private final Map<PageUpdateField, Serializable> fields;

    public PageUpdater() {
        fields = new HashMap<PageUpdateField, Serializable>(3);
    }

    public PageUpdater setName(final String name) {
        fields.put(PageUpdateField.NAME, name);
        return this;
    }

    public PageUpdater setDescription(final String description) {
        fields.put(PageUpdateField.DESCRIPTION, description);
        return this;
    }

    public PageUpdater setDisplayName(final String displayName) {
        fields.put(PageUpdateField.DISPLAY_NAME, displayName);
        return this;
    }

    public PageUpdater setContentName(final String contentName) {
        fields.put(PageUpdateField.CONTENT_NAME, contentName);
        return this;
    }

    public Map<PageUpdateField, Serializable> getFields() {
        return fields;
    }

}
