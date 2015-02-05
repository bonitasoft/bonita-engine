/*******************************************************************************
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
 * This library is free software; you can redistribute it and/or modify it under the terms
 * of the GNU Lesser General Public License as published by the Free Software Foundation
 * version 2.1 of the License.
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License along with this
 * program; if not, write to the Free Software Foundation, Inc., 51 Franklin Street, Fifth
 * Floor, Boston, MA 02110-1301, USA.
 ******************************************************************************/
package org.bonitasoft.engine.business.data.impl.filter;

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
        return !file.getAbsolutePath().contains("com" + File.separatorChar + "bonitasoft");
    }

    private boolean acceptSourceFile(final File file, final String name) {
        return name.endsWith(".java") && !file.getName().endsWith("DAOImpl.java") && notClientResource(file);
    }
}
