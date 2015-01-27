/*******************************************************************************
 * Copyright (C) 2014 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package org.bonitasoft.engine.business.application;

import org.bonitasoft.engine.exception.NotFoundException;


/**
 * @author Elias Ricken de Medeiros
 *
 */
public class ApplicationNotFoundException extends NotFoundException {

    private static final long serialVersionUID = -4073233652785845623L;

    public ApplicationNotFoundException(final long applicationId) {
        super("Unable to find the application with id '" + applicationId + "'");
    }

}
