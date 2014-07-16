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
        NAME, DISPLAY_NAME, DESCRIPTION, CONTENT_NAME;
    }

    private static final long serialVersionUID = 8174091386958635983L;

    private final Map<PageField, Serializable> fields;

    public PageCreator(final String name, final String contentName) {
        fields = new HashMap<PageField, Serializable>(2);
        fields.put(PageField.NAME, name);
        fields.put(PageField.CONTENT_NAME, contentName);
    }

    public String getName() {
        return fields.get(PageField.NAME).toString();
    }

    public PageCreator setDescription(final String description) {
        fields.put(PageField.DESCRIPTION, description);
        return this;
    }

    public PageCreator setDisplayName(final String displayName) {
        fields.put(PageField.DISPLAY_NAME, displayName);
        return this;
    }

    public Map<PageField, Serializable> getFields() {
        return fields;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (fields == null ? 0 : fields.hashCode());
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        PageCreator other = (PageCreator) obj;
        if (fields == null) {
            if (other.fields != null) {
                return false;
            }
        } else if (!fields.equals(other.fields)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "PageCreator [fields=" + fields + "]";
    }

}
