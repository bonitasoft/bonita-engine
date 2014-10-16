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
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Describes the information about an {@link ApplicationMenu} to be created
 *
 * @author Elias Ricken de Medeiros
 * @since 6.4
 * @see ApplicationMenu
 */
public class ApplicationMenuCreator implements Serializable {

    private static final long serialVersionUID = 5253969343647340983L;

    private final Map<ApplicationMenuField, Serializable> fields;

    /**
     * Creates an instance of {@code ApplicationMenuCreator}
     *
     * @param applicationId the identifier of related {@link Application}
     * @param displayName the {@link ApplicationMenu} display name
     * @param applicationPageId the identifier of related {@link ApplicationPage}
     * @param index the index indicating the menu position
     * @see ApplicationMenu
     */
    public ApplicationMenuCreator(final Long applicationId, final String displayName, final Long applicationPageId, final int index) {
        this(applicationId, displayName, index);
        fields.put(ApplicationMenuField.APPLICATION_PAGE_ID, applicationPageId);
    }

    /**
     * Creates an instance of {@code ApplicationMenuCreator}
     *
     * @param applicationId the identifier of related {@link com.bonitasoft.engine.business.application.Application}
     * @param displayName the {@link com.bonitasoft.engine.business.application.ApplicationMenu} display name
     * @param index the index indicating the menu position
     * @see ApplicationMenu
     */
    public ApplicationMenuCreator(final Long applicationId, final String displayName, final int index) {
        fields = new HashMap<ApplicationMenuField, Serializable>(4);
        fields.put(ApplicationMenuField.DISPLAY_NAME, displayName);
        fields.put(ApplicationMenuField.APPLICATION_ID, applicationId);
        fields.put(ApplicationMenuField.INDEX, index);
    }

    /**
     * Defines the identifier of parent {@link ApplicationMenu}
     *
     * @param parentId the identifier of parent {@code ApplicationMenu}
     * @return
     */
    public ApplicationMenuCreator setParentId(final long parentId) {
        fields.put(ApplicationMenuField.PARENT_ID, parentId);
        return this;
    }

    /**
     * Retrieves all fields defined in this {@code ApplicationMenuCreator}
     *
     * @return a {@link Map}<{@link ApplicationMenuField}, {@link Serializable}> containing all fields defined in this {@code ApplicationMenuCreator}
     */
    public Map<ApplicationMenuField, Serializable> getFields() {
        return Collections.unmodifiableMap(fields);
    }

}
