/*******************************************************************************
 * Copyright (C) 2015 BonitaSoft S.A.
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
 * @author Elias Ricken de Medeiros
 * @deprecated from version 7.0 on, use {@link org.bonitasoft.engine.business.application.ApplicationMenuUpdater} instead.
 * @see org.bonitasoft.engine.business.application.ApplicationMenuUpdater
 */
@Deprecated
public class ApplicationMenuUpdater implements Serializable {

    private org.bonitasoft.engine.business.application.ApplicationMenuUpdater delegate;

    public ApplicationMenuUpdater() {
        delegate = new org.bonitasoft.engine.business.application.ApplicationMenuUpdater();
    }

    /**
     * Retrieves all fields to be updated
     *
     * @return a {@link Map}&lt;{@link ApplicationMenuField}, {@link Serializable}&gt; containing all fields to be updated
     * @see ApplicationMenuField
     */
    public Map<ApplicationMenuField, Serializable> getFields() {
        return new EnumConverter().convert(delegate.getFields(), ApplicationMenuField.class);
    }

    /**
     * Defines the identifier of the new {@link ApplicationPage} related to the {@link com.bonitasoft.engine.business.application.ApplicationMenu}. Use
     * {@code null} to reference no {@code ApplicationPage}.
     *
     * @param applicationPageId the identifier of new related {@code ApplicationPage}
     * @return the current {@code ApplicationMenuUpdater}
     * @see com.bonitasoft.engine.business.application.ApplicationPage
     * @see com.bonitasoft.engine.business.application.ApplicationMenu
     */
    public ApplicationMenuUpdater setApplicationPageId(final Long applicationPageId) {
        delegate.setApplicationPageId(applicationPageId);
        return this;
    }

    /**
     * Defines the new value for the {@link ApplicationMenu} display name
     *
     * @param displayName the new value for the {@code ApplicationMenu} display name
     * @return the current {@code ApplicationMenuUpdater}
     * @see ApplicationMenu
     */
    public ApplicationMenuUpdater setDisplayName(final String displayName) {
        delegate.setDisplayName(displayName);
        return this;
    }

    /**
     * Defines the new value for the {@link ApplicationMenu} index
     *
     * @param index the new value for the {@code ApplicationMenu} index
     * @return the current {@code ApplicationMenuUpdater}
     * @see ApplicationMenu
     */
    public ApplicationMenuUpdater setIndex(final int index) {
        delegate.setIndex(index);
        return this;
    }

    /**
     * Defines the identifier of the new parent {@link ApplicationMenu}.Use {@code null} to reference no {@code ApplicationMenu}
     *
     * @param parentId the identifier of the new parent {@link ApplicationMenu}
     * @return the current {@code ApplicationMenuUpdater}
     * @see ApplicationMenu
     */
    public ApplicationMenuUpdater setParentId(final Long parentId) {
        delegate.setParentId(parentId);
        return this;
    }

    /**
     * Determines if this updater has at least one field to update
     *
     * @return true if there is at least one field to update; false otherwise
     */
    public boolean hasFields() {
        return !getFields().isEmpty();
    }

    public org.bonitasoft.engine.business.application.ApplicationMenuUpdater getDelegate() {
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
