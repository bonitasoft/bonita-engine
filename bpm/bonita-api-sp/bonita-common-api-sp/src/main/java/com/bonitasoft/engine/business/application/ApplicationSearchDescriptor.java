/*******************************************************************************
 * Copyright (C) 2014 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.business.application;

import org.bonitasoft.engine.search.SearchOptions;

import com.bonitasoft.engine.api.ApplicationAPI;

/**
 * Defines the fields that can be used in the {@link SearchOptions} when searching for {@link Application}s
 *
 * @author Elias Ricken de Medeiros
 * @since 6.4
 * @see SearchOptions
 * @see Application
 * @see ApplicationAPI#searchApplications(SearchOptions)
 */
public class ApplicationSearchDescriptor {

    /**
     * Used to filter or order by the <code>Application</code> identifier
     */
    public static final String ID = "id";

    /**
     * Used to filter or order by the <code>Application</code> name
     */
    public static final String NAME = "name";

    /**
     * Used to filter or order by the <code>Application</code> display name
     */
    public static final String DISPLAY_NAME = "displayName";

    /**
     * Used to filter or order by the <code>Application</code> version
     */
    public static final String VERSION = "version";

    /**
     * Used to filter or order by the <code>Application</code> path
     */
    public static final String PATH = "path";

    /**
     * Used to filter or order by the <code>Application</code> icon path
     */
    public static final String ICON_PATH = "iconPath";

    /**
     * Used to filter or order by the <code>Application</code> creation date
     */
    public static final String CREATION_DATE = "creationDate";

    /**
     * Used to filter or order by the identifier of the user that created the <code>Application</code>
     */
    public static final String CREATED_BY = "createdBy";

    /**
     * Used to filter or order by the <code>Application</code> last update date
     */
    public static final String LAST_UPDATE_DATE = "lastUpdateDate";

    /**
     * Used to filter or order by the identifier of the user that last updated the <code>Application</code>
     */
    public static final String UPDATED_BY = "updatedBy";

    /**
     * Used to filter or order by the <code>Application</code> state. The possible values are {@link ApplicationState#ACTIVATED#name()} and
     * {@link ApplicationState#DEACTIVATED
     * #name()}
     * 
     * @see ApplicationState
     */
    public static final String STATE = "state";

}
