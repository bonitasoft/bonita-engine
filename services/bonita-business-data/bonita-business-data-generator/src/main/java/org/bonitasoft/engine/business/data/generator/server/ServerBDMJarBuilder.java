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
package org.bonitasoft.engine.business.data.generator.server;

import java.io.File;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.bonitasoft.engine.bdm.model.BusinessObject;
import org.bonitasoft.engine.bdm.model.BusinessObjectModel;
import org.bonitasoft.engine.business.data.generator.AbstractBDMJarBuilder;
import org.bonitasoft.engine.business.data.generator.CodeGenerationException;
import org.bonitasoft.engine.business.data.generator.PersistenceUnitBuilder;
import org.bonitasoft.engine.business.data.generator.compiler.JDTCompiler;
import org.bonitasoft.engine.io.IOUtils;
import org.w3c.dom.Document;

/**
 * @author Matthieu Chaffotte
 * @author Romain Bioteau
 */
public class ServerBDMJarBuilder extends AbstractBDMJarBuilder {

    @Deprecated
    public ServerBDMJarBuilder(final JDTCompiler compiler) {
        super(new ServerBDMCodeGenerator(), compiler);
    }

    public ServerBDMJarBuilder() {
        super(new ServerBDMCodeGenerator());
    }

    @Override
    protected void addSourceFilesToDirectory(BusinessObjectModel bom, File directory) throws CodeGenerationException {
        super.addSourceFilesToDirectory(bom, directory);
        addPersistenceFile(directory, bom);
        addBOMFile(directory, bom);
    }

    /**
     * protected for testing - must be changed
     */
    protected void addPersistenceFile(final File directory, final BusinessObjectModel bom)
            throws CodeGenerationException {
        try {
            final List<BusinessObject> entities = bom.getBusinessObjects();
            final PersistenceUnitBuilder builder = new PersistenceUnitBuilder();
            for (final BusinessObject businessObject : entities) {
                builder.addClass(businessObject.getQualifiedName());
            }
            final Document document = builder.done();
            final File metaInf = IOUtils.createSubDirectory(directory, "META-INF");
            IOUtils.saveDocument(document, new File(metaInf, "persistence.xml"));
        } catch (Exception e) {
            throw new CodeGenerationException("Error when generating persistence.xml file", e);
        }
    }

    private void addBOMFile(final File directory, final BusinessObjectModel bom) throws CodeGenerationException {
        Path file = new File(directory, "bom.xml").toPath();
        try {
            final URL resource = BusinessObjectModel.class.getResource("/bom.xsd");
            final byte[] bomXML = IOUtils.marshallObjectToXML(bom, resource);
            Files.write(file, bomXML);
        } catch (Exception e) {
            throw new CodeGenerationException("Error when adding business object model metadata to server jar", e);
        }
    }

}
