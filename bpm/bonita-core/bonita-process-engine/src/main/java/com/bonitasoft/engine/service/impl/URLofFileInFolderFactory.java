/*******************************************************************************
 * Copyright (C) 2014 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.service.impl;

import java.io.File;

import org.springframework.beans.factory.config.AbstractFactoryBean;

/**
 * @author Baptiste Mesta
 * 
 */
public class URLofFileInFolderFactory extends AbstractFactoryBean<String> {

    private String filePath;

    private String baseFolder;

    @Override
    public Class<?> getObjectType() {
        return String.class;
    }

    @Override
    protected String createInstance() throws Exception {
        File file = new File(new File(baseFolder).getAbsolutePath(), filePath);
        if (file.exists()) {
            return file.toURI().toURL().toString();
        }
        throw new IllegalStateException("The file " + filePath + " does not exists in " + baseFolder);

    }

    public void setFilePath(final String filePath) {
        this.filePath = filePath;

    }

    public void setBaseFolder(final String baseFolder) {
        this.baseFolder = baseFolder;

    }

}
