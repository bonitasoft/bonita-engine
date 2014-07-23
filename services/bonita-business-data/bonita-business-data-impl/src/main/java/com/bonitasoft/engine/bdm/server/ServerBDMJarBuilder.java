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
import java.io.IOException;
import java.net.URL;
import java.util.List;

import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.bonitasoft.engine.commons.io.IOUtil;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import com.bonitasoft.engine.bdm.AbstractBDMCodeGenerator;
import com.bonitasoft.engine.bdm.AbstractBDMJarBuilder;
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
        super(compiler, dependencyPath);
    }

    @Override
    protected AbstractBDMCodeGenerator getBDMCodeGenerator(final BusinessObjectModel bom) {
        return new ServerBDMCodeGenerator(bom);
    }

    @Override
    protected void addPersistenceFile(final File directory, final BusinessObjectModel bom) throws IOException, TransformerException,
    ParserConfigurationException, SAXException {
        final List<BusinessObject> entities = bom.getBusinessObjects();
        final PersistenceUnitBuilder builder = new PersistenceUnitBuilder();
        for (final BusinessObject businessObject : entities) {
            builder.addClass(businessObject.getQualifiedName());
        }
        final Document document = builder.done();
        final File metaInf = IOUtils.createSubDirectory(directory, "META-INF");
        IOUtils.saveDocument(document, new File(metaInf, "persistence.xml"));
    }

    @Override
    protected void addBOMFile(final File directory, final BusinessObjectModel bom) throws IOException, JAXBException, SAXException {
        final URL resource = BusinessObjectModel.class.getResource("/bom.xsd");
        final byte[] bomXML = IOUtils.marshallObjectToXML(bom, resource);
        IOUtil.write(new File(directory, "bom.xml"), bomXML);
    }

    @Override
    protected void addClientResources(final File directory) throws ClassNotFoundException {
        // Do nothing
    }

}
