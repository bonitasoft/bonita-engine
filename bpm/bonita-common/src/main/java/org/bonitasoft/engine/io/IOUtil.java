/**
 * Copyright (C) 2023 Bonitasoft S.A.
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
package org.bonitasoft.engine.io;

import static java.util.Arrays.stream;
import static java.util.Collections.emptyList;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.URI;
import java.net.URL;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.SimpleJavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.StandardLocation;
import javax.tools.ToolProvider;

import lombok.extern.slf4j.Slf4j;

/**
 * @author Elias Ricken de Medeiros
 * @author Celine Souchet
 */
@Slf4j
public class IOUtil {

    private IOUtil() {
        // Utility class
    }

    public static final String TMP_DIRECTORY = System.getProperty("java.io.tmpdir");
    private static final int BUFFER_SIZE = 100000;
    private static final String CLASS_EXT = ".class";

    public static byte[] generateJar(final Class<?>... classes) throws IOException {
        return generateJar(getResources(classes));
    }

    public static byte[] generateJar(String className, String... content) throws IOException {
        return generateJar(emptyList(), new AbstractMap.SimpleEntry<>(className, String.join("\n", content)));
    }

    static class StringJavaFileObject extends SimpleJavaFileObject {

        private final String sourceCode;

        StringJavaFileObject(String className, String content) {
            super(URI.create("string:///" + className.replace('.', '/') + Kind.SOURCE.extension), Kind.SOURCE);
            this.sourceCode = content;
        }

        public CharSequence getCharContent(boolean var1) {
            return this.sourceCode;
        }
    }

    public static byte[] generateJar(Map.Entry<String, String>... classFiles) throws IOException {
        return generateJar(emptyList(), classFiles);
    }

    public static byte[] generateJar(List<Path> additionalJar, String className, String... content) throws IOException {
        return generateJar(additionalJar, new AbstractMap.SimpleEntry<>(className, String.join("\n", content)));
    }

    public static byte[] generateJar(List<Path> additionalJar, Map.Entry<String, String>... classFiles)
            throws IOException {
        List<StringJavaFileObject> sourceFiles = stream(classFiles).map(classFile -> {
            StringJavaFileObject sourceFile = new StringJavaFileObject(classFile.getKey(), classFile.getValue());
            return sourceFile;
        }).collect(Collectors.toList());

        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        StandardJavaFileManager standardFileManager = compiler.getStandardFileManager(null, null, null);
        updateClassPath(additionalJar, standardFileManager);
        standardFileManager.setLocation(StandardLocation.CLASS_OUTPUT,
                Collections.singleton(Files.createTempDirectory("compile-test").toFile()));
        JavaCompiler.CompilationTask run = compiler.getTask(null, standardFileManager, null, null, null,
                sourceFiles);
        Boolean call = run.call();
        if (!call) {
            throw new IllegalArgumentException("Unable to compile the file, see logs");
        }

        Map<String, byte[]> resources = new HashMap<>();
        for (Map.Entry<String, String> classFile : classFiles) {
            String className = classFile.getKey();
            JavaFileObject javaFileForInput = standardFileManager.getJavaFileForInput(StandardLocation.CLASS_OUTPUT,
                    className, JavaFileObject.Kind.CLASS);
            byte[] bytes = Files.readAllBytes(Paths.get(javaFileForInput.toUri()));
            resources.put(className.replace(".", "/") + ".class", bytes);
        }
        return generateJar(resources);
    }

    private static void updateClassPath(List<Path> additionalJar, StandardJavaFileManager standardFileManager)
            throws IOException {
        Iterable<? extends File> location = standardFileManager.getLocation(StandardLocation.CLASS_PATH);
        ArrayList<File> classPath = new ArrayList<>();
        location.forEach(classPath::add);
        additionalJar.stream().map(Path::toFile).forEach(classPath::add);
        standardFileManager.setLocation(StandardLocation.CLASS_PATH, classPath);
    }

    public static Map<String, byte[]> getResources(final Class<?>... classes) throws IOException {
        if (classes == null || classes.length == 0) {
            final String message = "No classes available";
            throw new IOException(message);
        }
        final Map<String, byte[]> resources = new HashMap<>();
        for (final Class<?> clazz : classes) {
            resources.put(clazz.getName().replace(".", "/") + CLASS_EXT, getClassData(clazz));
            for (final Class<?> internalClass : clazz.getDeclaredClasses()) {
                resources.put(internalClass.getName().replace(".", "/") + CLASS_EXT, getClassData(internalClass));
            }
        }
        return resources;
    }

    public static byte[] getClassData(final Class<?> clazz) throws IOException {
        if (clazz == null) {
            final String message = "Class is null";
            throw new IOException(message);
        }
        final String resource = clazz.getName().replace('.', '/') + CLASS_EXT;
        byte[] data;
        try (InputStream inputStream = clazz.getClassLoader().getResourceAsStream(resource)) {
            if (inputStream == null) {
                throw new IOException(
                        "Impossible to get stream from class: " + clazz.getName() + ", className= " + resource);
            }
            data = IOUtil.getAllContentFrom(inputStream);
        }
        return data;
    }

    public static byte[] generateJar(final Map<String, byte[]> resources) throws IOException {
        if (resources == null || resources.isEmpty()) {
            final String message = "No resources available";
            throw new IOException(message);
        }

        ByteArrayOutputStream baos = null;
        JarOutputStream jarOutStream = null;
        try {
            baos = new ByteArrayOutputStream();
            jarOutStream = new JarOutputStream(new BufferedOutputStream(baos));
            for (final Map.Entry<String, byte[]> resource : resources.entrySet()) {
                jarOutStream.putNextEntry(new JarEntry(resource.getKey()));
                jarOutStream.write(resource.getValue());
            }
            jarOutStream.flush();
            baos.flush();
        } finally {
            if (jarOutStream != null) {
                jarOutStream.close();
            }
            if (baos != null) {
                baos.close();
            }
        }

        return baos.toByteArray();
    }

    /**
     * Return the whole underlying stream content into a single String.
     * Warning: the whole content of stream will be kept in memory!! Use with
     * care!
     *
     * @param in the stream to read
     * @return the whole content of the stream in a single String.
     * @throws IOException if an I/O exception occurs
     */
    public static byte[] getAllContentFrom(final InputStream in) throws IOException {
        if (in == null) {
            throw new IOException("The InputStream is null!");
        }
        final byte[] buffer = new byte[BUFFER_SIZE];
        final byte[] resultArray;

        try (BufferedInputStream bis = new BufferedInputStream(in);
                ByteArrayOutputStream result = new ByteArrayOutputStream()) {
            int amountRead;
            while ((amountRead = bis.read(buffer)) > 0) {
                result.write(buffer, 0, amountRead);
            }
            resultArray = result.toByteArray();
            result.flush();
        }
        return resultArray;
    }

    /**
     * Equivalent to {@link #getAllContentFrom(InputStream) getAllContentFrom(new
     * FileInputStream(file))};
     *
     * @param file the file to read
     * @return the whole content of the file in a single String.
     * @throws IOException If an I/O exception occurs
     */
    public static byte[] getAllContentFrom(final File file) throws IOException {
        try (InputStream in = new FileInputStream(file)) {
            return getAllContentFrom(in);
        }
    }

    /**
     * Return the whole underlying stream content into a single String.
     * Warning: the whole content of stream will be kept in memory!! Use with
     * care!
     *
     * @param url the URL to read
     * @return the whole content of the stream in a single String.
     * @throws IOException if an I/O exception occurs
     */
    public static byte[] getAllContentFrom(final URL url) throws IOException {
        try (InputStream in = url.openStream()) {
            return getAllContentFrom(in);
        }
    }

    public static File createTempDirectory(final URI directoryPath) {
        final File tmpDir = new File(directoryPath);
        tmpDir.setReadable(true);
        tmpDir.setWritable(true);

        mkdirs(tmpDir);

        Files.isSymbolicLink(tmpDir.toPath());

        try {
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                try {
                    final boolean deleted = deleteDir(tmpDir);
                    if (!deleted) {
                        var errorMsg = "Unable to delete directory: " + tmpDir;
                        log.error(errorMsg);
                        if (!tmpDir.exists()) {
                            throw new FileNotFoundException("Directory does not exist: " + tmpDir);
                        }
                        throw new IOException(errorMsg);
                    }
                } catch (final IOException e) {
                    throw new RuntimeException(e);
                }
            }));
        } catch (IllegalStateException ignored) {
            // happen in case of hook already registered and when shutting down
        }
        return tmpDir;
    }

    public static boolean deleteDir(final File dir) throws IOException {
        return deleteDir(dir, 1, 0);
    }

    public static boolean deleteDir(final File dir, final int attempts, final long sleepTime) throws IOException {
        if (dir != null) {
            boolean result = true;
            if (!dir.exists()) {
                return true; //already deleted
            }
            if (!dir.isDirectory()) {
                throw new IOException("Unable to delete directory: " + dir + ", it is not a directory");
            }
            for (final File file : dir.listFiles()) {
                if (file.isDirectory()) {
                    result &= deleteDir(file, attempts, sleepTime);
                } else {
                    result &= deleteFile(file, attempts, sleepTime);
                }
            }
            return result && deleteFile(dir, attempts, sleepTime);
        }
        return false;
    }

    public static boolean deleteFile(final File f, final int attempts, final long sleepTime) {
        int retries = attempts;
        while (retries > 0) {
            if (f.delete()) {
                break;
            }
            retries--;
            try {
                Thread.sleep(sleepTime);
            } catch (final InterruptedException ignored) {
            }
        }
        return retries > 0;
    }

    public static byte[] zip(final Map<String, byte[]> files) throws IOException {
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ZipOutputStream zos = new ZipOutputStream(baos)) {
            for (final Entry<String, byte[]> file : files.entrySet()) {
                zos.putNextEntry(new ZipEntry(file.getKey()));
                zos.write(file.getValue());
                zos.closeEntry();
            }
            return baos.toByteArray();
        }
    }

    public static void unzipToFolder(final InputStream inputStream, final File outputFolder) throws IOException {
        try (ZipInputStream zipInputstream = new ZipInputStream(inputStream)) {
            extractZipEntries(zipInputstream, outputFolder);
        }
    }

    private static void mkdirs(final File file) {
        if (!file.exists()) {
            file.mkdirs();
        }
    }

    private static void extractZipEntries(final ZipInputStream zipInputstream, final File outputFolder)
            throws IOException {
        ZipEntry zipEntry;
        while ((zipEntry = zipInputstream.getNextEntry()) != null) {
            try {
                // For each entry, a file is created in the output directory "folder"
                final File outputFile = new File(outputFolder.getAbsolutePath(), zipEntry.getName());
                // If the entry is a directory, it creates in the output folder, and we go to the next entry (continue).
                if (zipEntry.isDirectory()) {
                    mkdirs(outputFile);
                    continue;
                }
                writeZipInputToFile(zipInputstream, outputFile);
            } finally {
                zipInputstream.closeEntry();
            }
        }
    }

    private static void writeZipInputToFile(final ZipInputStream zipInputstream, final File outputFile)
            throws FileNotFoundException, IOException {
        // The input is a file. An FileOutputStream is created to write the content of the new file.
        mkdirs(outputFile.getParentFile());
        try (FileOutputStream fileOutputStream = new FileOutputStream(outputFile)) {
            // The contents of the new file, that is read from the ZipInputStream using a buffer (byte []), is written.
            int bytesRead;
            final byte[] buffer = new byte[BUFFER_SIZE];
            while ((bytesRead = zipInputstream.read(buffer)) > -1) {
                fileOutputStream.write(buffer, 0, bytesRead);
            }
            fileOutputStream.flush();
        } catch (final IOException ioe) {
            // In case of error, the file is deleted
            outputFile.delete();
            throw ioe;
        }
    }

    public static void writeContentToFile(final String content, final File outputFile) throws IOException {
        final FileOutputStream fileOutput = new FileOutputStream(outputFile);
        writeContentToFileOutputStream(content, fileOutput);
    }

    public static void writeContentToFileOutputStream(final String content, final FileOutputStream fileOutput)
            throws IOException {
        try (OutputStreamWriter out = new OutputStreamWriter(fileOutput, StandardCharsets.UTF_8)) {
            out.write(content);
            out.flush();
        } finally {
            fileOutput.close();
        }
    }

    public static void write(final File file, final byte[] fileContent) throws IOException {
        try (FileOutputStream fos = new FileOutputStream(file);
                BufferedOutputStream bos = new BufferedOutputStream(fos)) {
            bos.write(fileContent);
            bos.flush();
        }
    }

    public static byte[] getContent(final File file) throws IOException {
        try (FileInputStream fin = new FileInputStream(file);
                FileChannel ch = fin.getChannel()) {
            final int size = (int) ch.size();
            final MappedByteBuffer buf = ch.map(MapMode.READ_ONLY, 0, size);
            final byte[] bytes = new byte[size];
            buf.get(bytes);
            return bytes;
        }
    }

}
