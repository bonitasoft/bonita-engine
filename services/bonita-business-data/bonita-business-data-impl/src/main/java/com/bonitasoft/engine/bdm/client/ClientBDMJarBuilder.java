/*******************************************************************************
 * Copyright (C) 2014 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.bdm.client;

import java.io.File;
import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.xml.sax.SAXException;

import com.bonitasoft.engine.bdm.AbstractBDMJarBuilder;
import com.bonitasoft.engine.bdm.BDMCompiler;
import com.bonitasoft.engine.bdm.BusinessObjectModel;
import com.bonitasoft.engine.bdm.BusinessObjectModelValidationException;
import com.sun.codemodel.JClassAlreadyExistsException;

/**
 * @author Matthieu Chaffotte
 */
public class ClientBDMJarBuilder extends AbstractBDMJarBuilder {

    public ClientBDMJarBuilder(final BDMCompiler compiler) {
        super(compiler);
    }

    protected void generateJavaFiles(final BusinessObjectModel bom, final File directory) throws IOException, JClassAlreadyExistsException,
            BusinessObjectModelValidationException, ClassNotFoundException {
        ClientBDMCodeGenerator codeGenerator = new ClientBDMCodeGenerator(bom);
        codeGenerator.generate(directory);
    }

    protected void addPersistenceFile(final File directory, final BusinessObjectModel bom) throws IOException, TransformerException,
            ParserConfigurationException, SAXException {
        // DO NOTHING
    }

}
