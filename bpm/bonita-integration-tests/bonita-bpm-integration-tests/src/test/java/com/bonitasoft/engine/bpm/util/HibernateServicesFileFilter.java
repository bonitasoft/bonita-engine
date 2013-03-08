/*******************************************************************************
 * Copyright (C) 2009, 2013 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.bpm.util;

import java.io.File;
import java.io.FileFilter;

/**
 * @author Matthieu Chaffotte
 */
public class HibernateServicesFileFilter implements FileFilter {

    @Override
    public boolean accept(final File pathname) {
        return pathname.isFile() && pathname.getName().endsWith(".xml") && !pathname.getName().contains("mybatis");
    }

}
