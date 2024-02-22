/**
 * Copyright (C) 2021 Bonitasoft S.A.
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
package org.bonitasoft.engine.classloader;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.tuple.Pair;
import org.bonitasoft.engine.commons.io.IOUtil;
import org.bonitasoft.engine.exception.BonitaRuntimeException;
import org.bonitasoft.engine.home.BonitaResource;

public class BonitaClassLoaderFactory {

    private static final Pattern jarMatcher = Pattern.compile(".*\\.jar");

    static BonitaClassLoader createClassLoader(Stream<BonitaResource> resources, ClassLoaderIdentifier id,
            URI temporaryDirectoryUri,
            ClassLoader parent) throws IOException {
        File temporaryDirectory = createTemporaryDirectory(temporaryDirectoryUri);
        Map<String, File> allFiles = writeResourcesOnFileSystem(resources, temporaryDirectory);
        Set<File> jars = allFiles.entrySet().stream().filter(u -> jarMatcher.matcher(u.getKey()).matches())
                .map(Map.Entry::getValue).collect(Collectors.toSet());
        Map<String, File> nonJarResources = allFiles.entrySet().stream()
                .filter(u -> !jarMatcher.matcher(u.getKey()).matches())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        return new BonitaClassLoader(id, parent, jars, nonJarResources, temporaryDirectory);
    }

    private static File createTemporaryDirectory(URI temporaryDirectoryUri) throws IOException {
        Path temporaryDirectory = new File(temporaryDirectoryUri).toPath();
        if (!Files.exists(temporaryDirectory)) {
            Files.createDirectory(temporaryDirectory);
        }
        return Files.createTempDirectory(temporaryDirectory, "engine-classloader").toFile();
    }

    private static Map<String, File> writeResourcesOnFileSystem(final Stream<BonitaResource> resources,
            File temporaryDirectory) {
        return resources.map(resource -> {
            try {
                return writeResource(resource, temporaryDirectory);
            } catch (final IOException e) {
                throw new BonitaRuntimeException(e);
            }
        }).collect(Collectors.toMap(Pair::getKey, Pair::getValue));
    }

    static Pair<String, File> writeResource(BonitaResource resource, File temporaryDirectory) throws IOException {
        String name = resource.getName();
        int i = name.lastIndexOf(".");
        final File file = File.createTempFile(i < 3 ? "tmp" : name.substring(0, i), i < 3 ? name : name.substring(i),
                temporaryDirectory);
        IOUtil.write(file, resource.getContent());
        return Pair.of(name, file);
    }
}
