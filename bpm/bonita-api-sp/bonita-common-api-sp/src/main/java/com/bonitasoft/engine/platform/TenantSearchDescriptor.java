/*******************************************************************************
 * Copyright (C) 2013 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.platform;

/**
 * Constants to search tenants using {@link com.bonitasoft.engine.api.PlatformAPI#searchTenants(org.bonitasoft.engine.search.SearchOptions)}
 *
 * @author Zhao Na
 * @since 6.0.0
 */
public class TenantSearchDescriptor {

    public static final String ID = "id";

    public static final String NAME = "name";

    public static final String DESCRIPTION = "description";

    public static final String ICON_NAME = "iconName";

    public static final String ICON_PATH = "iconPath";

    public static final String STATUS = "status";

    public static final String CREATION_DATE = "created";

    public static final String CREATED_BY = "createdBy";

    public static final String DEFAULT_TENANT = "defaultTenant";

}
