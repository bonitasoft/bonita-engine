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
import java.io.InputStream;
import java.net.URL;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.bonitasoft.engine.commons.io.IOUtil;
import org.xml.sax.SAXException;

import com.bonitasoft.engine.bdm.model.BusinessObjectModel;
import com.bonitasoft.engine.business.data.SBusinessDataRepositoryDeploymentException;
import com.bonitasoft.engine.compiler.JDTCompiler;
import com.bonitasoft.engine.io.IOUtils;
import com.sun.codemodel.JClassAlreadyExistsException;

/**
 * @author Matthieu Chaffotte
 */
public abstract class AbstractBDMJarBuilder {

    private final JDTCompiler compiler;

    private final String dependencyPath;

    public AbstractBDMJarBuilder(final JDTCompiler compiler, final String dependencyPath) {
        this.compiler = compiler;
        this.dependencyPath = dependencyPath == null ? "" : dependencyPath;
    }

    /**
     * @param bom
     * @param fileFilter
     *            filter the entries to be added or not in generated jar
     * @return the content of the generated jar
     * @throws SBusinessDataRepositoryDeploymentException
     */
    public byte[] build(final BusinessObjectModel bom, final IOFileFilter fileFilter) throws SBusinessDataRepositoryDeploymentException {
        try {
            final File tmpBDMDirectory = createBDMTmpDir();
            try {
                generateJavaFiles(bom, tmpBDMDirectory);
                compiler.compile(tmpBDMDirectory, new File(dependencyPath));
                addPersistenceFile(tmpBDMDirectory, bom);
                addBOMFile(tmpBDMDirectory, bom);
                return generateJar(tmpBDMDirectory, fileFilter);
            } finally {
                FileUtils.deleteDirectory(tmpBDMDirectory);
            }
        } catch (final Exception e) {
            throw new SBusinessDataRepositoryDeploymentException(e);
        }
    }

    protected File createBDMTmpDir() throws IOException {
        return IOUtils.createTempDirectory("bdm");
    }

    protected byte[] generateJar(final File directory, final IOFileFilter fileFilter) throws IOException {
        final Collection<File> files = FileUtils.listFiles(directory, fileFilter, TrueFileFilter.TRUE);
        final Map<String, byte[]> resources = new HashMap<String, byte[]>();
        for (final File file : files) {
            final String relativeName = directory.toURI().relativize(file.toURI()).getPath();
            final byte[] content = FileUtils.readFileToByteArray(file);
            resources.put(relativeName, content);
        }
        return IOUtil.generateJar(resources);
    }

    protected void generateJavaFiles(final BusinessObjectModel bom, final File directory) throws IOException, JClassAlreadyExistsException,
    BusinessObjectModelValidationException, ClassNotFoundException {
        final AbstractBDMCodeGenerator codeGenerator = getBDMCodeGenerator(bom);
        codeGenerator.generate(directory);
        addClientResources(directory);
    }

    protected abstract void addClientResources(final File directory) throws ClassNotFoundException, IOException;

    protected void addResourceForClass(final File directory, final String className) throws ClassNotFoundException, IOException {
        final String resourceName = className.replace(".", "/") + ".java";
        final URL resource = AbstractBDMJarBuilder.class.getResource("/" + resourceName);
        if (resource == null) {
            throw new IllegalArgumentException(resourceName + " not found in classloader");
        }
        final String packageName = toPackagePath(className);
        final File packageDirectory = new File(directory,packageName);
        if(!packageDirectory.exists()){
            packageDirectory.mkdirs();
        }
        final File sourceFile = new File(packageDirectory, toSourceFilename(className));
        InputStream openStream = null;
        try {
            openStream = resource.openStream();
            IOUtil.write(sourceFile, IOUtil.getAllContentFrom(openStream));
        } finally {
            if (openStream != null) {
                openStream.close();
            }
        }
    }

    private String toSourceFilename(final String className) {
        final String sourceFilename = className.substring(className.lastIndexOf(".") + 1, className.length());
        return sourceFilename + ".java";
    }

    private String toPackagePath(final String className) {
        final String packagePath = className.substring(0,className.lastIndexOf("."));
        return packagePath.replace(".", File.separator);
    }

    protected abstract void addPersistenceFile(final File directory, final BusinessObjectModel bom) throws IOException, TransformerException,
    ParserConfigurationException, SAXException;

    protected abstract void addBOMFile(final File directory, BusinessObjectModel bom) throws IOException, JAXBException, SAXException;

    protected abstract AbstractBDMCodeGenerator getBDMCodeGenerator(BusinessObjectModel bom);

}
