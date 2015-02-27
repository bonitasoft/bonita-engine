/*******************************************************************************
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.bdm;

import static com.bonitasoft.engine.io.IOUtils.createTempDirectory;
import static org.apache.commons.io.FileUtils.deleteDirectory;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.bonitasoft.engine.commons.io.IOUtil;

import com.bonitasoft.engine.bdm.model.BusinessObjectModel;
import com.bonitasoft.engine.business.data.SBusinessDataRepositoryDeploymentException;
import com.bonitasoft.engine.compiler.JDTCompiler;

/**
 * @author Matthieu Chaffotte
 */
public abstract class AbstractBDMJarBuilder {

    private final JDTCompiler compiler;

    private final String dependencyPath;

    private AbstractBDMCodeGenerator bdmCodeGenerator;

    public AbstractBDMJarBuilder(AbstractBDMCodeGenerator bdmCodeGenerator, final JDTCompiler compiler, final String dependencyPath) {
        this.bdmCodeGenerator = bdmCodeGenerator;
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
            final File tmpBDMDirectory = createTempDirectory("bdm");
            try {
                addSourceFilesToDirectory(bom, tmpBDMDirectory);
                compiler.compile(tmpBDMDirectory, new File(dependencyPath));
                return generateJar(tmpBDMDirectory, fileFilter);
            } finally {
                deleteDirectory(tmpBDMDirectory);
            }
        } catch (final Exception e) {
            throw new SBusinessDataRepositoryDeploymentException(e);
        }
    }

    protected void addSourceFilesToDirectory(final BusinessObjectModel bom, final File directory) throws CodeGenerationException {
        try {
            bdmCodeGenerator.generateBom(bom, directory);
        } catch (Exception e) {
            throw new CodeGenerationException("Error when generating source files for business object model", e);
        }
    }

    private byte[] generateJar(final File directory, final IOFileFilter fileFilter) throws IOException {
        final Collection<File> files = FileUtils.listFiles(directory, fileFilter, TrueFileFilter.TRUE);
        final Map<String, byte[]> resources = new HashMap<String, byte[]>();
        for (final File file : files) {
            final String relativeName = directory.toURI().relativize(file.toURI()).getPath();
            final byte[] content = FileUtils.readFileToByteArray(file);
            resources.put(relativeName, content);
        }
        return IOUtil.generateJar(resources);
    }

    
}
