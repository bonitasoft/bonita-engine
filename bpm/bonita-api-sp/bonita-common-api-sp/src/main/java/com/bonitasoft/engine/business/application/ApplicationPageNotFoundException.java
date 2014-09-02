/*******************************************************************************
 * Copyright (C) 2014 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.business.application;

import org.bonitasoft.engine.exception.NotFoundException;


/**
 * @author Elias Ricken de Medeiros
 *
 */
public class ApplicationPageNotFoundException extends NotFoundException {

    private static final long serialVersionUID = -5011088434149162619L;

    public ApplicationPageNotFoundException(final String message) {
        super(message);
    }

}
