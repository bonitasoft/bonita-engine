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
package org.bonitasoft.engine.bdm;

import static org.bonitasoft.engine.io.IOUtils.createTempDirectory;
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

import org.bonitasoft.engine.bdm.model.BusinessObjectModel;
import org.bonitasoft.engine.business.data.SBusinessDataRepositoryDeploymentException;
import org.bonitasoft.engine.compiler.JDTCompiler;

/**
 * @author Matthieu Chaffotte
 */
public abstract class AbstractBDMJarBuilder {

    private final JDTCompiler compiler;

    private AbstractBDMCodeGenerator bdmCodeGenerator;

    public AbstractBDMJarBuilder(AbstractBDMCodeGenerator bdmCodeGenerator, final JDTCompiler compiler) {
        this.bdmCodeGenerator = bdmCodeGenerator;
        this.compiler = compiler;
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
                compiler.compile(tmpBDMDirectory, Thread.currentThread().getContextClassLoader());
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
