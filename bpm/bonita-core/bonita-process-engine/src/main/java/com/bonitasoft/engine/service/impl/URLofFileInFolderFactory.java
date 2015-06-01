/*******************************************************************************
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.service.impl;

import java.io.File;
import java.net.MalformedURLException;

import org.springframework.beans.factory.config.AbstractFactoryBean;

/**
 * @author Baptiste Mesta
 * 
 */
public class URLofFileInFolderFactory extends AbstractFactoryBean<String> {

    private String primaryFilePath;

    private String secondaryFilePath;

    private String baseFolder;

    @Override
    public Class<?> getObjectType() {
        return String.class;
    }

    @Override
    protected String createInstance() throws Exception {
        final String folder = new File(baseFolder).getAbsolutePath();
        File primaryFile = new File(folder, primaryFilePath);
        if (primaryFile.exists()) {
            return url(primaryFile);
        }
        File secondaryFile = new File(folder, secondaryFilePath);
        if (secondaryFile.exists()) {
            return url(secondaryFile);
        }
        throw new IllegalStateException("Neither primary file '" + primaryFilePath + "', neither secondary file '" + secondaryFilePath +  "' exists in folder '" + folder + "'");

    }

    private String url(final File file) throws MalformedURLException {
        return file.toURI().toURL().toString();
    }

    public void setPrimaryFilePath(final String primaryFilePath) {
        this.primaryFilePath = primaryFilePath;

    }

    public void setSecondaryFilePath(final String secondaryFilePath) {
        this.secondaryFilePath = secondaryFilePath;

    }

    public void setBaseFolder(final String baseFolder) {
        this.baseFolder = baseFolder;

    }

}
