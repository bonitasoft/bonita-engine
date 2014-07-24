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
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import org.bonitasoft.engine.commons.io.IOUtil;

import com.bonitasoft.engine.bdm.AbstractBDMJarBuilder;
import com.bonitasoft.engine.bdm.CodeGenerationException;
import com.bonitasoft.engine.bdm.model.BusinessObjectModel;
import com.bonitasoft.engine.compiler.JDTCompiler;

/**
 * @author Romain Bioteau
 */
public class ClientBDMJarBuilder extends AbstractBDMJarBuilder {

    public ClientBDMJarBuilder(final JDTCompiler compiler, final String dependencyPath) {
        super(new ClientBDMCodeGenerator(), compiler, dependencyPath);
    }

    @Override
    protected void addSourceFilesToDirectory(BusinessObjectModel bom, File directory) throws CodeGenerationException {
        super.addSourceFilesToDirectory(bom, directory);
        addClientResources(directory);
    }
    
    private void addClientResources(final File directory) throws CodeGenerationException {
        try {
            addResourceForClass(directory, "com.bonitasoft.engine.bdm.dao.proxy.LazyLoader");
            addResourceForClass(directory, "com.bonitasoft.engine.bdm.dao.proxy.Proxyfier");
            addResourceForClass(directory, "com.bonitasoft.engine.bdm.dao.BusinessObjectDeserializer");
            addResourceForClass(directory, "com.bonitasoft.engine.bdm.dao.utils.Capitalizer");
            addResourceForClass(directory, "com.bonitasoft.engine.bdm.dao.utils.BDMQueryCommandParameters");
            addResourceForClass(directory, "com.bonitasoft.engine.bdm.dao.utils.EntityGetter");
        } catch (Exception e) {
            throw new CodeGenerationException("Error when adding compilation dependencies to client jar", e);
        }
    }

    private void addResourceForClass(final File directory, final String className) throws ClassNotFoundException, IOException {
        final String resourceName = className.replace(".", "/") + ".java";
        final URL resource = AbstractBDMJarBuilder.class.getResource("/" + resourceName);
        if (resource == null) {
            throw new IllegalArgumentException(resourceName + " not found in classloader");
        }
        final String packageName = toPackagePath(className);
        final File packageDirectory = new File(directory,packageName);
        if(!packageDirectory.exists()){
            packageDirectory.mkdirs();
        }
        final File sourceFile = new File(packageDirectory, toSourceFilename(className));
        InputStream openStream = null;
        try {
            openStream = resource.openStream();
            IOUtil.write(sourceFile, IOUtil.getAllContentFrom(openStream));
        } finally {
            if (openStream != null) {
                openStream.close();
            }
        }
    }

    private String toSourceFilename(final String className) {
        final String sourceFilename = className.substring(className.lastIndexOf(".") + 1, className.length());
        return sourceFilename + ".java";
    }

    private String toPackagePath(final String className) {
        final String packagePath = className.substring(0,className.lastIndexOf("."));
        return packagePath.replace(".", File.separator);
    }
}
