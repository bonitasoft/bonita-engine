/*******************************************************************************
 * Copyright (C) 2015 BonitaSoft S.A.
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
 * Defines the fields that can be used in the {@link SearchOptions} when searching for {@link ApplicationPage}s
 *
 * @author Elias Ricken de Medeiros
 * @since 6.4
 * @see ApplicationPage
 * @see SearchOptions
 * @see ApplicationAPI#searchApplicationPages(SearchOptions)
 * @see org.bonitasoft.engine.business.application.ApplicationPageSearchDescriptor
 * @deprecated from version 7.0 on, use {@link org.bonitasoft.engine.business.application.ApplicationPageSearchDescriptor} instead.
 */
@Deprecated
public class ApplicationPageSearchDescriptor extends org.bonitasoft.engine.business.application.ApplicationPageSearchDescriptor {

}
