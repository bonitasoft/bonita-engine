/*******************************************************************************
 * Copyright (C) 2014 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.business.application;

import org.bonitasoft.engine.bpm.BaseElement;

import com.bonitasoft.engine.page.Page;

/**
 * Associates the given {@link Page} to the given {@link Application}
 *
 * @author Elias Ricken de Medeiros
 */
public interface ApplicationPage extends BaseElement {

    long getApplicationId();

    long getPageId();

    String getName();

}
