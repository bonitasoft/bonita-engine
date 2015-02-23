/*******************************************************************************
 * Copyright (C) 2013, 2014 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.business.data.impl.filter;

import java.io.File;

import org.apache.commons.io.filefilter.AbstractFileFilter;

/**
 * @author Romain Bioteau
 */
public class WithoutDAOImplementationFileFilter extends AbstractFileFilter {

    @Override
    public boolean accept(final File file) {
        final String name = file.getName();
        return acceptClassFile(file, name) || acceptSourceFile(file, name);
    }

    private boolean acceptClassFile(final File file, final String name) {
        return name.endsWith(".class") && !file.getName().endsWith("DAOImpl.class") && notClientResource(file);
    }

    private boolean notClientResource(final File file) {
        return !file.getAbsolutePath().contains("com" + File.separatorChar + "bonitasoft"+ File.separatorChar);
    }

    private boolean acceptSourceFile(final File file, final String name) {
        return name.endsWith(".java") && !file.getName().endsWith("DAOImpl.java") && notClientResource(file);
    }
}
