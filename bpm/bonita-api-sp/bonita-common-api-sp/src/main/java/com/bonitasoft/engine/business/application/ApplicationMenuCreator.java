/*******************************************************************************
 * Copyright (C) 2014 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.business.application;

import java.io.Serializable;
import java.util.Map;

import com.bonitasoft.engine.converter.EnumConverter;

/**
 * Describes the information about an {@link ApplicationMenu} to be created
 *
 * @author Elias Ricken de Medeiros
 * @since 6.4
 * @deprecated from version 7.0 on, use {@link org.bonitasoft.engine.business.application.ApplicationMenuCreator} instead.
 * @see ApplicationMenu
 * @see org.bonitasoft.engine.business.application.ApplicationMenuCreator
 */
@Deprecated
public class ApplicationMenuCreator implements Serializable {

    private static final long serialVersionUID = 5253969343647340983L;

    private org.bonitasoft.engine.business.application.ApplicationMenuCreator delegate;

    /**
     * Creates an instance of {@code ApplicationMenuCreator}
     *
     * @param applicationId the identifier of related {@link com.bonitasoft.engine.business.application.Application}
     * @param displayName the {@link com.bonitasoft.engine.business.application.ApplicationMenu} display name
     * @param applicationPageId the identifier of related {@link com.bonitasoft.engine.business.application.ApplicationPage}
     * @see ApplicationMenu
     */
    public ApplicationMenuCreator(final Long applicationId, final String displayName, final Long applicationPageId) {
        delegate = new org.bonitasoft.engine.business.application.ApplicationMenuCreator(applicationId, displayName, applicationPageId);
    }

    /**
     * Creates an instance of {@code ApplicationMenuCreator}
     *
     * @param applicationId the identifier of related {@link Application}
     * @param displayName the {@link ApplicationMenu} display name
     * @see ApplicationMenu
     * @see com.bonitasoft.engine.business.application.Application
     */
    public ApplicationMenuCreator(final Long applicationId, final String displayName) {
        delegate = new org.bonitasoft.engine.business.application.ApplicationMenuCreator(applicationId, displayName);
    }

    /**
     * Defines the identifier of parent {@link ApplicationMenu}
     *
     * @param parentId the identifier of parent {@code ApplicationMenu}
     * @return
     */
    public ApplicationMenuCreator setParentId(final long parentId) {
        delegate.setParentId(parentId);
        return this;
    }

    /**
     * Retrieves the identifier of the parent {@link ApplicationMenu}. If no parent is defined this method will return null.
     * 
     * @return the identifier of the parent {@code ApplicationMenu} or null if no parent is defined
     * @see com.bonitasoft.engine.business.application.ApplicationMenu
     */
    public Long getParentId() {
        return delegate.getParentId();
    }

    /**
     * Retrieves all fields defined in this {@code ApplicationMenuCreator}
     *
     * @return a {@link Map}<{@link ApplicationMenuField}, {@link Serializable}> containing all fields defined in this {@code ApplicationMenuCreator}
     */
    public Map<ApplicationMenuField, Serializable> getFields() {
        return new EnumConverter().convert(delegate.getFields(), ApplicationMenuField.class);
    }

    public org.bonitasoft.engine.business.application.ApplicationMenuCreator getDelegate() {
        return delegate;
    }

    @Override
    public boolean equals(final Object obj) {
        return delegate.equals(obj);
    }

    @Override
    public int hashCode() {
        return delegate.hashCode();
    }

}
