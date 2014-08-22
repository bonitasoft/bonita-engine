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
import java.util.HashMap;
import java.util.Map;

/**
 * @author Elias Ricken de Medeiros
 *
 */
public class ApplicationMenuCreator {

    public enum ApplicationMenuField {
        DISPLAY_NAME, APPLICATION_ID, PARENT_ID, POSITION;
    }

    private final Map<ApplicationMenuField, Serializable> fields;

    public ApplicationMenuCreator(final String displayName, final long applicationPageId, final String position) {
        fields = new HashMap<ApplicationMenuField, Serializable>(2);
        fields.put(ApplicationMenuField.DISPLAY_NAME, displayName);
        fields.put(ApplicationMenuField.APPLICATION_ID, applicationPageId);
        fields.put(ApplicationMenuField.POSITION, position);
    }

    public ApplicationMenuCreator setParentId(final long parentId) {
        fields.put(ApplicationMenuField.PARENT_ID, parentId);
        return this;
    }

    public Map<ApplicationMenuField, Serializable> getFields() {
        return fields;
    }

}
