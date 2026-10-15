/**
 * Copyright (C) 2019 Bonitasoft S.A.
 * Bonitasoft, 32 rue Gustave Eiffel - 38000 Grenoble
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
package org.bonitasoft.engine.business.data.generator;

import static org.apache.commons.io.FileUtils.deleteDirectory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.persistence.Entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.bonitasoft.engine.bdm.model.BusinessObjectModel;
import org.bonitasoft.engine.business.data.generator.compiler.JDTCompiler;
import org.bonitasoft.engine.commons.exceptions.SBonitaRuntimeException;
import org.bonitasoft.engine.commons.io.IOUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Matthieu Chaffotte
 */
public abstract class AbstractBDMJarBuilder {

    private static final Logger log = LoggerFactory.getLogger(AbstractBDMJarBuilder.class);
    private final JDTCompiler compiler;

    private final AbstractBDMCodeGenerator bdmCodeGenerator;

    @Deprecated
    protected AbstractBDMJarBuilder(AbstractBDMCodeGenerator bdmCodeGenerator, final JDTCompiler compiler) {
        this.bdmCodeGenerator = bdmCodeGenerator;
        this.compiler = compiler;
    }

    protected AbstractBDMJarBuilder(AbstractBDMCodeGenerator bdmCodeGenerator) {
        this.bdmCodeGenerator = bdmCodeGenerator;
        this.compiler = new JDTCompiler();
    }

    /**
     * @param bom the business object model to generate the jar from
     * @param fileFilter
     *        filter the entries to be added or not in generated jar
     * @return the content of the generated jar
     * @throws BDMJarGenerationException if an error occurs during the generation of the jar
     */
    public byte[] build(final BusinessObjectModel bom, final IOFileFilter fileFilter) throws BDMJarGenerationException {
        try {
            final File tmpBDMDirectory = Files.createTempDirectory("bdm").toFile();
            try {
                addSourceFilesToDirectory(bom, tmpBDMDirectory);
                var additionalClasspath = getCompileDependencies();
                if (log.isDebugEnabled()) {
                    log.debug("Compiling BDM classes using classpath: {}",
                            additionalClasspath.stream()
                                    .map(File::getName)
                                    .collect(Collectors.joining(File.pathSeparator)));
                }
                compiler.compile(tmpBDMDirectory, tmpBDMDirectory, additionalClasspath.toArray(File[]::new));
                return generateJar(tmpBDMDirectory, fileFilter);
            } finally {
                deleteDirectory(tmpBDMDirectory);
            }
        } catch (final Exception e) {
            throw new BDMJarGenerationException(e);
        }
    }

    /**
     * Add BDM compile dependencies required to compile the BDM classes.
     * It uses the current classloader to find the required classes and retrieve their corresponding jar files.
     *
     * @throws ClassNotFoundException if a required class cannot be found in the current classloader
     */
    protected Set<File> getCompileDependencies() throws ClassNotFoundException {
        var compileDependencies = new HashSet<File>();
        compileDependencies.add(JDTCompiler.lookupJarContaining(Entity.class));
        compileDependencies.add(JDTCompiler.lookupJarContaining(org.bonitasoft.engine.bdm.Entity.class));
        compileDependencies.add(JDTCompiler.lookupJarContaining(JsonIgnore.class));
        compileDependencies.add(JDTCompiler.lookupJarContaining("org.hibernate.annotations.Parameter"));
        compileDependencies.add(JDTCompiler.lookupJarContaining(DateConverter.class));
        compileDependencies.add(JDTCompiler.lookupJarContaining(SBonitaRuntimeException.class));
        compileDependencies
                .add(JDTCompiler.lookupJarContaining("org.bonitasoft.engine.business.data.BusinessDataRepository"));
        return compileDependencies;
    }

    protected void addSourceFilesToDirectory(final BusinessObjectModel bom, final File directory)
            throws CodeGenerationException {
        try {
            bdmCodeGenerator.generateBom(bom, directory);
        } catch (Exception e) {
            throw new CodeGenerationException("Error when generating source files for business object model", e);
        }
    }

    private byte[] generateJar(final File directory, final IOFileFilter fileFilter) throws IOException {
        final Collection<File> files = FileUtils.listFiles(directory, fileFilter, TrueFileFilter.TRUE);
        final Map<String, byte[]> resources = new HashMap<>();
        for (final File file : files) {
            final String relativeName = directory.toURI().relativize(file.toURI()).getPath();
            final byte[] content = FileUtils.readFileToByteArray(file);
            resources.put(relativeName, content);
        }
        return IOUtil.generateJar(resources);
    }

}
