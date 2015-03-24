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
package org.bonitasoft.engine.bdm.server;

import java.io.File;
import java.net.URL;
import java.util.List;

import org.bonitasoft.engine.commons.io.IOUtil;
import org.w3c.dom.Document;

import org.bonitasoft.engine.bdm.AbstractBDMJarBuilder;
import org.bonitasoft.engine.bdm.CodeGenerationException;
import org.bonitasoft.engine.bdm.model.BusinessObject;
import org.bonitasoft.engine.bdm.model.BusinessObjectModel;
import org.bonitasoft.engine.business.data.impl.PersistenceUnitBuilder;
import org.bonitasoft.engine.compiler.JDTCompiler;
import org.bonitasoft.engine.io.IOUtils;

/**
 * @author Matthieu Chaffotte
 * @author Romain Bioteau
 */
public class ServerBDMJarBuilder extends AbstractBDMJarBuilder {

    public ServerBDMJarBuilder(final JDTCompiler compiler) {
        super(new ServerBDMCodeGenerator(), compiler);
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
    protected void addPersistenceFile(final File directory, final BusinessObjectModel bom) throws CodeGenerationException {
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
        try {
            final URL resource = BusinessObjectModel.class.getResource("/bom.xsd");
            final byte[] bomXML = IOUtils.marshallObjectToXML(bom, resource);
            IOUtil.write(new File(directory, "bom.xml"), bomXML);
        } catch (Exception e) {
            throw new CodeGenerationException("Error when adding business object model metadata to server jar", e);
        }
    }

}
