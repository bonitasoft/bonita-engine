/*******************************************************************************
 * Copyright (C) 2015 BonitaSoft S.A.
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
public class OnlyDAOImplementationFileFilter extends AbstractFileFilter {

    @Override
    public boolean accept(final File file) {
        final String name = file.getName();
        return name.endsWith("DAOImpl.class") || file.getAbsolutePath().contains("com" + File.separator + "bonitasoft");
    }
}
