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
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import com.bonitasoft.engine.bdm.AbstractBDMJarBuilder;
import com.bonitasoft.engine.bdm.BDMCompiler;
import com.bonitasoft.engine.bdm.BusinessObject;
import com.bonitasoft.engine.bdm.BusinessObjectModel;
import com.bonitasoft.engine.bdm.BusinessObjectModelValidationException;
import com.bonitasoft.engine.business.data.impl.PersistenceUnitBuilder;
import com.bonitasoft.engine.io.IOUtils;
import com.sun.codemodel.JClassAlreadyExistsException;

/**
 * @author Matthieu Chaffotte
 * @author Romain Bioteau
 */
public class ServerBDMJarBuilder extends AbstractBDMJarBuilder {

    public ServerBDMJarBuilder(final BDMCompiler compiler) {
        super(compiler);
    }

    protected void generateJavaFiles(final BusinessObjectModel bom, final File directory) throws IOException, JClassAlreadyExistsException,
            BusinessObjectModelValidationException, ClassNotFoundException {
        ServerBDMCodeGenerator codeGenerator = new ServerBDMCodeGenerator(bom);
        codeGenerator.generate(directory);
    }

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

}
