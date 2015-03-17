/**
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
 **/
package org.bonitasoft.engine.bdm.client;

import java.io.File;

import org.bonitasoft.engine.bdm.AbstractBDMJarBuilder;
import org.bonitasoft.engine.bdm.CodeGenerationException;
import org.bonitasoft.engine.bdm.model.BusinessObjectModel;
import org.bonitasoft.engine.compiler.JDTCompiler;

/**
 * @author Romain Bioteau
 */
public class ClientBDMJarBuilder extends AbstractBDMJarBuilder {

    private ResourcesLoader resourcesLoader;

    public ClientBDMJarBuilder(final JDTCompiler compiler, ResourcesLoader resourcesLoader) {
        super(new ClientBDMCodeGenerator(), compiler);
        this.resourcesLoader = resourcesLoader;
    }

    @Override
    protected void addSourceFilesToDirectory(BusinessObjectModel bom, File directory) throws CodeGenerationException {
        super.addSourceFilesToDirectory(bom, directory);
        addClientResources(directory);
    }
    
    private void addClientResources(final File directory) throws CodeGenerationException {
        try {
            resourcesLoader.copyJavaFilesToDirectory("org.bonitasoft.engine.bdm.dao.client.resources", directory);
        } catch (Exception e) {
            throw new CodeGenerationException("Error when adding compilation dependencies to client jar", e);
        }
    }
}
