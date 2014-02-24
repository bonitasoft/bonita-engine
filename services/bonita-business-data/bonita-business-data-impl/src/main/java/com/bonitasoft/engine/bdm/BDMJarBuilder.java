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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.apache.commons.io.FileUtils;
import org.bonitasoft.engine.commons.io.IOUtil;
import org.xml.sax.SAXException;

import com.bonitasoft.engine.business.data.SBusinessDataRepositoryDeploymentException;
import com.bonitasoft.engine.business.data.impl.PersistenceUnitBuilder;
import com.sun.codemodel.JClassAlreadyExistsException;

/**
 * @author Matthieu Chaffotte
 */
public class BDMJarBuilder {

    private final byte[] bdmZip;

    public BDMJarBuilder(final byte[] bdmZip) {
        this.bdmZip = bdmZip;
    }

    public byte[] build() throws SBusinessDataRepositoryDeploymentException {
        final BusinessObjectModelConverter converter = new BusinessObjectModelConverter();
        try {
            final BusinessObjectModel bom = converter.unzip(bdmZip);
            final File tmpBDMDirectory = generateJavaClasses(bom);
            compileJavaClasses(tmpBDMDirectory);
            final byte[] jar = com.bonitasoft.engine.io.IOUtils.toJar(tmpBDMDirectory.getAbsolutePath());
            FileUtils.deleteDirectory(tmpBDMDirectory);
            return addPersistenceFile(jar, bom);
        } catch (final SBusinessDataRepositoryDeploymentException sbdrde) {
            throw sbdrde;
        } catch (final Exception e) {
            throw new SBusinessDataRepositoryDeploymentException(e);
        }
    }

    protected File generateJavaClasses(final BusinessObjectModel bom) throws IOException, JClassAlreadyExistsException {
        final BDMCodeGenerator codeGenerator = new BDMCodeGenerator(bom);
        final File tmpBDMDirectory = File.createTempFile("bdm", null);
        tmpBDMDirectory.delete();
        tmpBDMDirectory.mkdir();
        codeGenerator.generate(tmpBDMDirectory);
        return tmpBDMDirectory;
    }

    protected void compileJavaClasses(final File srcDirectory) throws SBusinessDataRepositoryDeploymentException {
        final Collection<File> files = FileUtils.listFiles(srcDirectory, new String[] { "java" }, true);
        final JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        final StandardJavaFileManager fileManager = compiler.getStandardFileManager(null, null, null);
        final Iterable<? extends JavaFileObject> compUnits = fileManager.getJavaFileObjectsFromFiles(files);
        final DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<JavaFileObject>();
        final List<String> optionList = new ArrayList<String>();
        // set compiler's classpath to be same as the runtime's
        optionList.addAll(Arrays.asList("-classpath", System.getProperty("java.class.path")));
        final Boolean compiled = compiler.getTask(null, fileManager, diagnostics, optionList, null, compUnits).call();
        if (!compiled) {
            final StringBuilder sb = new StringBuilder();
            for (final Diagnostic<? extends JavaFileObject> diagnostic : diagnostics.getDiagnostics()) {
                sb.append(diagnostic.getMessage(Locale.getDefault()));
                sb.append("\n");
            }
            throw new SBusinessDataRepositoryDeploymentException("bdm compilation process fails:" + sb.toString());
        }
    }

    protected byte[] addPersistenceFile(final byte[] jar, final BusinessObjectModel bom) throws IOException, TransformerException,
            ParserConfigurationException, SAXException {
        final List<BusinessObject> entities = bom.getEntities();
        final PersistenceUnitBuilder builder = new PersistenceUnitBuilder();
        for (final BusinessObject businessObject : entities) {
            builder.addClass(businessObject.getQualifiedName());
        }
        final byte[] persistenceFileContent = IOUtil.toByteArray(builder.done());
        return IOUtil.addJarEntry(jar, "META-INF/persistence.xml", persistenceFileContent);
    }

}
