/*******************************************************************************
 * Copyright (C) 2014 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.bdm.client;

import java.io.File;

import com.bonitasoft.engine.bdm.AbstractBDMJarBuilder;
import com.bonitasoft.engine.bdm.CodeGenerationException;
import com.bonitasoft.engine.bdm.model.BusinessObjectModel;
import com.bonitasoft.engine.compiler.JDTCompiler;

/**
 * @author Romain Bioteau
 */
public class ClientBDMJarBuilder extends AbstractBDMJarBuilder {

    private ResourcesLoader resourcesLoader;

    public ClientBDMJarBuilder(final JDTCompiler compiler, ResourcesLoader resourcesLoader, final String dependencyPath) {
        super(new ClientBDMCodeGenerator(), compiler, dependencyPath);
        this.resourcesLoader = resourcesLoader;
    }

    @Override
    protected void addSourceFilesToDirectory(BusinessObjectModel bom, File directory) throws CodeGenerationException {
        super.addSourceFilesToDirectory(bom, directory);
        addClientResources(directory);
    }
    
    private void addClientResources(final File directory) throws CodeGenerationException {
        try {
            resourcesLoader.copyJavaFilesToDirectory("com.bonitasoft.engine.bdm.dao.client.resources", directory);
        } catch (Exception e) {
            throw new CodeGenerationException("Error when adding compilation dependencies to client jar", e);
        }
    }
}
