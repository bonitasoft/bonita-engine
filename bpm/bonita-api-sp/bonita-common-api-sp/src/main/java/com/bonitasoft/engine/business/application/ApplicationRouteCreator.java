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
public class ApplicationRouteCreator implements Serializable {

    private static final long serialVersionUID = 6243567846450837708L;

    public enum ApplicationRouteField {

        APPLICATION_ID,

        TOKEN,

        PARENT_ROUTE_ID,

        PAGE_ID,

        MENU_NAME,

        MENU_INDEX,

        MENU_VISIBILITY;
    }

    private final Map<ApplicationRouteField, Serializable> fields;

    public ApplicationRouteCreator(final String token, final long applicationId, final int index, final String menuName) {
        fields = new HashMap<ApplicationRouteField, Serializable>(2);
        fields.put(ApplicationRouteField.APPLICATION_ID, applicationId);
        fields.put(ApplicationRouteField.TOKEN, token);
        fields.put(ApplicationRouteField.MENU_INDEX, index);
        fields.put(ApplicationRouteField.MENU_NAME, menuName);
        fields.put(ApplicationRouteField.MENU_VISIBILITY, true);
    }

    public ApplicationRouteCreator setParentRouteId(final long parentRouteId) {
        fields.put(ApplicationRouteField.PARENT_ROUTE_ID, parentRouteId);
        return this;
    }

    public ApplicationRouteCreator setPageId(final long pageId) {
        fields.put(ApplicationRouteField.PAGE_ID, pageId);
        return this;
    }

    public ApplicationRouteCreator setMenuVisibility(final boolean visibility) {
        fields.put(ApplicationRouteField.MENU_VISIBILITY, visibility);
        return this;
    }

    public Map<ApplicationRouteField, Serializable> getFields() {
        return fields;
    }

}
