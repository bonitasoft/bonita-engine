/*******************************************************************************
 * Copyright (C) 2009, 2013 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.core.page;

import org.bonitasoft.engine.commons.exceptions.SBonitaException;

/**
 * @author Laurent Leseigneur
 */
public class SPageNotFoundException extends SBonitaException {

    private static final long serialVersionUID = 4494354293903396785L;

    public SPageNotFoundException(final long pageId) {
        super("the page with id:'" + pageId + "' does not exist");
    }

}
