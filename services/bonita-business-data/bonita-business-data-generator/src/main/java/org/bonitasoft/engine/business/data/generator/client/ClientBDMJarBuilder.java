/**
 * Copyright (C) 2019 Bonitasoft S.A.
 * Bonitasoft, 32 rue Gustave Eiffel - 38000 Grenoble
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
package org.bonitasoft.engine.business.data.generator.client;

import java.io.File;
import java.util.HashSet;
import java.util.Set;
import javassist.util.proxy.MethodHandler;

import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.bonitasoft.engine.bdm.model.BusinessObjectModel;
import org.bonitasoft.engine.bdm.model.field.Field;
import org.bonitasoft.engine.business.data.generator.AbstractBDMJarBuilder;
import org.bonitasoft.engine.business.data.generator.CodeGenerationException;
import org.bonitasoft.engine.business.data.generator.compiler.JDTCompiler;

/**
 * @author Romain Bioteau
 */
public class ClientBDMJarBuilder extends AbstractBDMJarBuilder {

    private ResourcesLoader resourcesLoader;

    @Deprecated
    public ClientBDMJarBuilder(final JDTCompiler compiler, ResourcesLoader resourcesLoader) {
        super(new ClientBDMCodeGenerator(), compiler);
        this.resourcesLoader = resourcesLoader;
    }

    public ClientBDMJarBuilder(ResourcesLoader resourcesLoader) {
        super(new ClientBDMCodeGenerator());
        this.resourcesLoader = resourcesLoader;
    }

    @Override
    protected Set<File> getCompileDependencies() throws ClassNotFoundException {
        var compileDependencies = new HashSet<>(super.getCompileDependencies());
        // Add dependencies to compile client jar
        compileDependencies.add(JDTCompiler.lookupJarContaining("org.bonitasoft.engine.api.TenantAPIAccessor"));
        compileDependencies.add(JDTCompiler.lookupJarContaining(
                "org.bonitasoft.engine.bdm.dao.client.resources.BusinessObjectDeserializer"));
        compileDependencies.add(JDTCompiler.lookupJarContaining(MethodHandler.class));
        compileDependencies.add(JDTCompiler.lookupJarContaining(ObjectMapper.class));
        compileDependencies.add(JDTCompiler.lookupJarContaining(ObjectCodec.class));
        compileDependencies.add(JDTCompiler.lookupJarContaining(Field.class));
        return compileDependencies;
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
