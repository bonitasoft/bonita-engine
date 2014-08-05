/*******************************************************************************
 * Copyright (C) 2014 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.bdm.server;

import java.io.File;
import java.net.URL;
import java.util.List;

import org.bonitasoft.engine.commons.io.IOUtil;
import org.w3c.dom.Document;

import com.bonitasoft.engine.bdm.AbstractBDMJarBuilder;
import com.bonitasoft.engine.bdm.CodeGenerationException;
import com.bonitasoft.engine.bdm.model.BusinessObject;
import com.bonitasoft.engine.bdm.model.BusinessObjectModel;
import com.bonitasoft.engine.business.data.impl.PersistenceUnitBuilder;
import com.bonitasoft.engine.compiler.JDTCompiler;
import com.bonitasoft.engine.io.IOUtils;

/**
 * @author Matthieu Chaffotte
 * @author Romain Bioteau
 */
public class ServerBDMJarBuilder extends AbstractBDMJarBuilder {

    public ServerBDMJarBuilder(final JDTCompiler compiler, final String dependencyPath) {
        super(new ServerBDMCodeGenerator(), compiler, dependencyPath);
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
