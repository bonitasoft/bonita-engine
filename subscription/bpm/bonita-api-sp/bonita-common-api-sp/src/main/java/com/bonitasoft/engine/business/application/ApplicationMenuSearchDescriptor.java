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
 * Defines the fields that can be used in the {@link SearchOptions} when searching for {@link ApplicationMenu}s
 *
 * @author Elias Ricken de Medeiros
 * @since 6.4
 *
 * @see SearchOptions
 * @see ApplicationMenu
 * @see ApplicationAPI#searchApplicationMenus(SearchOptions)
 * @see org.bonitasoft.engine.business.application.ApplicationMenuSearchDescriptor
 *
 *  @deprecated from version 7.0 on, use {@link org.bonitasoft.engine.business.application.ApplicationMenuSearchDescriptor} instead.
 */
@Deprecated
public class ApplicationMenuSearchDescriptor extends org.bonitasoft.engine.business.application.ApplicationMenuSearchDescriptor {

}
