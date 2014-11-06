/*******************************************************************************
 * Copyright (C) 2014 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 ******************************************************************************/

package com.bonitasoft.engine.business.application.importer;

import com.bonitasoft.engine.business.application.model.SApplication;
import org.bonitasoft.engine.exception.ExecutionException;

/**
 * @author Elias Ricken de Medeiros
 */
public class FailOnDuplicateApplicationImportStrategy implements ApplicationImportStrategy{

    @Override
    public void whenApplicationExists(SApplication existing, SApplication toBeImported) throws ExecutionException {
        throw new ExecutionException("An application with token '" + existing.getToken() + "' already exists");
    }

}
