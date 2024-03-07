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
package org.bonitasoft.engine.service.impl.installation;

import static java.util.stream.Collectors.toMap;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ConfigurationArchive implements AutoCloseable {

    private static final String PARAMETERS_FILENAME = "parameters.properties";
    private final File tmpFile;
    private String builderVersion;
    private String targetEnvironment;
    private final List<ProcessConfiguration> processConfigurations = new ArrayList<>();

    public ConfigurationArchive(byte[] content) throws IOException {
        tmpFile = File.createTempFile("configuration", ".zip");
        try (ByteArrayInputStream is = new ByteArrayInputStream(content)) {
            Files.copy(is, tmpFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        }
        try (ZipFile zipFile = new ZipFile(tmpFile)) {
            readManifest(zipFile);
            loadProcessConfigurations(zipFile);
            loadProcessConfigurationsParameters(zipFile);
        }
    }

    private void loadProcessConfigurationsParameters(ZipFile zipFile) throws IOException {
        for (ProcessConfiguration processConf : processConfigurations) {
            ZipEntry parametersEntry = zipFile
                    .getEntry(String.format("%s/%s/%s", processConf.getName(), processConf.getVersion(),
                            PARAMETERS_FILENAME));
            if (parametersEntry != null) {
                try (InputStream is = zipFile.getInputStream(parametersEntry)) {
                    Properties parameters = new Properties();
                    parameters.load(is);
                    processConf.setParameters(asMap(parameters.entrySet()));
                }
            }
        }

    }

    private Map<String, String> asMap(Set<Entry<Object, Object>> entrySet) {
        return entrySet.stream()
                .collect(toMap(entry -> (String) entry.getKey(), entry -> (String) entry.getValue(), (a, b) -> b));
    }

    private void loadProcessConfigurations(ZipFile zipFile) {
        zipFile.stream().forEach(entry -> {
            String name = entry.getName();
            String[] path = name.split("/");
            if (path.length > 2) {
                String processName = path[0];
                String processVersion = path[1];
                ProcessConfiguration processConfiguration = new ProcessConfiguration(processName, processVersion);
                if (!processConfigurations.contains(processConfiguration)) {
                    processConfigurations.add(processConfiguration);
                }
            }
        });

    }

    private void readManifest(ZipFile zipFile) throws IOException {
        ZipEntry manifestEntry = zipFile.getEntry("MANIFEST");
        if (manifestEntry == null) {
            throw new IOException("Invalid archive format (missing MANIFEST).");
        }
        try (InputStream is = zipFile.getInputStream(manifestEntry)) {
            Properties manifest = new Properties();
            manifest.load(is);
            builderVersion = manifest.getProperty("builder.version");
            targetEnvironment = manifest.getProperty("target.environment");
        }
    }

    public String getBuilderVersion() {
        return builderVersion;
    }

    public String getTargetEnvironment() {
        return targetEnvironment;
    }

    public List<ProcessConfiguration> getProcessConfigurations() {
        return processConfigurations;
    }

    @Override
    public void close() throws Exception {
        if (tmpFile != null) {
            log.debug("deleting temp file for application configuration file read");
            final boolean done = Files.deleteIfExists(tmpFile.toPath());
            log.debug("deleting temp file {}", done ? "successful" : "unsuccessful!");
        }
    }

}
