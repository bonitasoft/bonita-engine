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
 *         FIXME move in correct project
 */
public class PageCreatorImpl implements Serializable {

    private static final long serialVersionUID = 8174091386958635983L;

    public enum PageField {
        NAME, DESCRIPTION, URL_TOKEN;
    }

    private final Map<PageField, Serializable> fields;

    public PageCreatorImpl(final String name, final String urlToken) {
        fields = new HashMap<PageField, Serializable>(3);
        fields.put(PageField.NAME, name);
        fields.put(PageField.URL_TOKEN, urlToken);
        

    }

    public PageCreatorImpl setDescription(final String description) {
        fields.put(PageField.DESCRIPTION, description);
        return this;
    }

    public Map<PageField, Serializable> getFields() {
        return fields;
    }

}
