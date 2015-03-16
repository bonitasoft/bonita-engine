/*******************************************************************************
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.page;

import java.io.Serializable;
import java.util.Map;

import com.bonitasoft.engine.converter.EnumConverter;

/**
 * @author Laurent Leseigneur
 * @author Emmanuel Duchastenier
 * @deprecated from version 7.0 on, use {@link org.bonitasoft.engine.page.PageCreator} instead.
 */
@Deprecated
public class PageCreator implements Serializable {

    public enum PageField {
        NAME, DISPLAY_NAME, DESCRIPTION, CONTENT_NAME;
    }

    private static final long serialVersionUID = 8174091386958635983L;

    private final org.bonitasoft.engine.page.PageCreator delegate;

    public PageCreator(final String name, final String contentName) {
        delegate = new org.bonitasoft.engine.page.PageCreator(name, contentName);
    }

    public String getName() {
        return delegate.getName();
    }

    public PageCreator setDescription(final String description) {
        delegate.setDescription(description);
        return this;
    }

    public PageCreator setDisplayName(final String displayName) {
        delegate.setDisplayName(displayName);
        return this;
    }

    public Map<PageField, Serializable> getFields() {
        return new EnumConverter().convert(delegate.getFields(), PageField.class);
    }

    @Override
    public int hashCode() {
        return delegate.hashCode();
    }

    @Override
    public boolean equals(final Object obj) {
        return delegate.equals(obj);
    }

    public org.bonitasoft.engine.page.PageCreator getDelegate() {
        return delegate;
    }

}
