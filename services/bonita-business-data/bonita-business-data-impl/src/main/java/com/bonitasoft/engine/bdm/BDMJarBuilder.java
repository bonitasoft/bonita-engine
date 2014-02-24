/*******************************************************************************
 * Copyright (C) 2014 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.bdm;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.apache.commons.io.FileUtils;
import org.bonitasoft.engine.commons.io.IOUtil;
import org.xml.sax.SAXException;

import com.bonitasoft.engine.business.data.SBusinessDataRepositoryDeploymentException;
import com.bonitasoft.engine.business.data.impl.PersistenceUnitBuilder;
import com.bonitasoft.engine.compiler.CompilationException;
import com.bonitasoft.engine.compiler.JDTCompiler;
import com.sun.codemodel.JClassAlreadyExistsException;

/**
 * @author Matthieu Chaffotte
 */
public class BDMJarBuilder {

    private JDTCompiler jdtCompiler;

    public BDMJarBuilder(JDTCompiler jdtCompiler) {
        this.jdtCompiler = jdtCompiler;
    }

    public byte[] build(final byte[] bdmZip) throws SBusinessDataRepositoryDeploymentException {
        final BusinessObjectModelConverter converter = new BusinessObjectModelConverter();
        try {
            final BusinessObjectModel bom = converter.unzip(bdmZip);
            final File tmpBDMDirectory = generateJavaClasses(bom);
            compileJavaClasses(tmpBDMDirectory);
            final byte[] jar = com.bonitasoft.engine.io.IOUtils.toJar(tmpBDMDirectory.getAbsolutePath());
            FileUtils.deleteDirectory(tmpBDMDirectory);
            return addPersistenceFile(jar, bom);
        } catch (final Exception e) {
            throw new SBusinessDataRepositoryDeploymentException(e);
        }
    }

    private File generateJavaClasses(final BusinessObjectModel bom) throws IOException, JClassAlreadyExistsException {
        final BDMCodeGenerator codeGenerator = new BDMCodeGenerator(bom);
        final File tmpBDMDirectory = File.createTempFile("bdm", null);
        tmpBDMDirectory.delete();
        tmpBDMDirectory.mkdir();
        codeGenerator.generate(tmpBDMDirectory);
        return tmpBDMDirectory;
    }

    private void compileJavaClasses(final File srcDirectory) throws CompilationException {
        final Collection<File> files = FileUtils.listFiles(srcDirectory, new String[] { "java" }, true);
        jdtCompiler.compile(files, srcDirectory);
    }

    private byte[] addPersistenceFile(final byte[] jar, final BusinessObjectModel bom) throws IOException, TransformerException, ParserConfigurationException,
            SAXException {
        final List<BusinessObject> entities = bom.getEntities();
        final PersistenceUnitBuilder builder = new PersistenceUnitBuilder();
        for (final BusinessObject businessObject : entities) {
            builder.addClass(businessObject.getQualifiedName());
        }
        final byte[] persistenceFileContent = IOUtil.toByteArray(builder.done());
        return IOUtil.addJarEntry(jar, "META-INF/persistence.xml", persistenceFileContent);
    }

}
