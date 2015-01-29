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

/**
 * Defines the fields that can be used in the {@link org.bonitasoft.engine.search.SearchOptions} when searching for {@link Application}s
 *
 * @author Elias Ricken de Medeiros
 * @since 6.4
 * @see org.bonitasoft.engine.search.SearchOptions
 * @see Application
 * @see com.bonitasoft.engine.api.ApplicationAPI#searchApplications(SearchOptions)
 * @deprecated as from version 7.0, use {@link org.bonitasoft.engine.business.application.ApplicationSearchDescriptor} instead
 */
@Deprecated
public class ApplicationSearchDescriptor extends org.bonitasoft.engine.business.application.ApplicationSearchDescriptor {
}
