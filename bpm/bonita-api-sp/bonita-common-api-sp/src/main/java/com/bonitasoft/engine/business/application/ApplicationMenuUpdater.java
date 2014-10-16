/*******************************************************************************
 * Copyright (C) 2014 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 ******************************************************************************/

package com.bonitasoft.engine.business.application;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Elias Ricken de Medeiros
 */
public class ApplicationMenuUpdater {

    private final Map<ApplicationMenuField, Serializable> fields;

    public ApplicationMenuUpdater() {
        fields = new HashMap<ApplicationMenuField, Serializable>(4);
    }

    /**
    * Retrieves all fields to be updated
    *
    * @return a {@link Map}<{@link ApplicationMenuField}, {@link Serializable}> containing all fields to be updated
    * @see ApplicationMenuField
    */
    public Map<ApplicationMenuField, Serializable> getFields() {
        return fields;
    }

    /**
     * Defines the new value for the {@link ApplicationPage} id
     *
     * @param applicationPageId the new value for the {@code ApplicationPage} id
     * @return the current {@code ApplicationMenuUpdater}
     * @see ApplicationMenuField
     */
    public ApplicationMenuUpdater setApplicationPageId(final Long applicationPageId) {
        fields.put(ApplicationMenuField.APPLICATION_PAGE_ID, applicationPageId);
        return this;
    }

    /**
     * Defines the new value for the {@link ApplicationMenu} display name
     *
     * @param displayName the new value for the {@code ApplicationMenu} display name
     * @return the current {@code ApplicationMenuUpdater}
     * @see ApplicationMenuField
     */
    public ApplicationMenuUpdater setDispalyName(final String displayName) {
        fields.put(ApplicationMenuField.DISPLAY_NAME, displayName);
        return this;
    }

    /**
     * Defines the new value for the {@link ApplicationMenu} index
     *
     * @param index the new value for the {@code ApplicationMenu} index
     * @return the current {@code ApplicationMenuUpdater}
     * @see ApplicationMenuField
     */
    public ApplicationMenuUpdater setIndex(final int index) {
        fields.put(ApplicationMenuField.INDEX, index);
        return this;
    }

    /**
     * Defines the new value for the {@link ApplicationMenu} parent
     *
     * @param parentId the new value for the {@code ApplicationMenu} parent
     * @return the current {@code ApplicationMenuUpdater}
     * @see ApplicationMenuField
     */
    public ApplicationMenuUpdater setParentId(final Long parentId) {
        fields.put(ApplicationMenuField.PARENT_ID, parentId);
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
}
