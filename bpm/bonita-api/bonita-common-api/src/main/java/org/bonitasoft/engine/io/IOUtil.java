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
package org.bonitasoft.engine.io;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.lang.management.ManagementFactory;
import java.net.URI;
import java.net.URL;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.UUID;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.bonitasoft.engine.exception.BonitaRuntimeException;
import org.xml.sax.SAXException;

/**
 * @author Elias Ricken de Medeiros
 * @author Celine Souchet
 */
public class IOUtil {

    private static final String LINE_SEPARATOR = System.getProperty("line.separator");

    public static final String TMP_DIRECTORY = System.getProperty("java.io.tmpdir");

    private static final int BUFFER_SIZE = 100000;

    public static final String FILE_ENCODING = "UTF-8";

    private static final String JVM_NAME = ManagementFactory.getRuntimeMXBean().getName();

    public static byte[] generateJar(final Class<?>... classes) throws IOException {
        return generateJar(getResources(classes));
    }

    public static Map<String, byte[]> getResources(final Class<?>... classes) throws IOException {
        if (classes == null || classes.length == 0) {
            final String message = "No classes available";
            throw new IOException(message);
        }
        final Map<String, byte[]> resources = new HashMap<String, byte[]>();
        for (final Class<?> clazz : classes) {
            resources.put(clazz.getName().replace(".", "/") + ".class", getClassData(clazz));
            for (final Class<?> internalClass : clazz.getDeclaredClasses()) {
                resources.put(internalClass.getName().replace(".", "/") + ".class", getClassData(internalClass));
            }
        }
        return resources;
    }

    public static byte[] getClassData(final Class<?> clazz) throws IOException {
        if (clazz == null) {
            final String message = "Class is null";
            throw new IOException(message);
        }
        final String resource = clazz.getName().replace('.', '/') + ".class";
        final InputStream inputStream = clazz.getClassLoader().getResourceAsStream(resource);
        byte[] data = null;
        try {
            if (inputStream == null) {
                throw new IOException("Impossible to get stream from class: " + clazz.getName() + ", className= " + resource);
            }
            data = IOUtil.getAllContentFrom(inputStream);
        } finally {
            if (inputStream != null) {
                inputStream.close();
            }
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
     * @param in
     *        the stream to read
     * @return the whole content of the stream in a single String.
     * @throws IOException
     *         if an I/O exception occurs
     */
    public static byte[] getAllContentFrom(final InputStream in) throws IOException {
        if (in == null) {
            throw new IOException("The InputStream is null!");
        }
        final byte[] buffer = new byte[BUFFER_SIZE];
        final byte[] resultArray;
        BufferedInputStream bis = null;
        ByteArrayOutputStream result = null;

        try {
            bis = new BufferedInputStream(in);
            result = new ByteArrayOutputStream();
            int amountRead;
            while ((amountRead = bis.read(buffer)) > 0) {
                result.write(buffer, 0, amountRead);
            }
            resultArray = result.toByteArray();
            result.flush();

        } finally {
            if (bis != null) {
                bis.close();
            }
            if (result != null) {
                result.close();
            }
        }
        return resultArray;
    }

    /**
     * Equivalent to {@link #getAllContentFrom(InputStream) getAllContentFrom(new
     * FileInputStream(file))};
     *
     * @param file
     *        the file to read
     * @return the whole content of the file in a single String.
     * @throws IOException
     *         If an I/O exception occurs
     */
    public static byte[] getAllContentFrom(final File file) throws IOException {
        InputStream in = null;
        try {
            in = new FileInputStream(file);
            return getAllContentFrom(in);
        } finally {
            if (in != null) {
                in.close();
            }
        }
    }

    /**
     * Return the whole underlying stream content into a single String.
     * Warning: the whole content of stream will be kept in memory!! Use with
     * care!
     *
     * @param url
     *        the URL to read
     * @return the whole content of the stream in a single String.
     * @throws IOException
     *         if an I/O exception occurs
     */
    public static byte[] getAllContentFrom(final URL url) throws IOException {
        final InputStream in = url.openStream();
        try {
            return getAllContentFrom(in);
        } finally {
            in.close();
        }
    }

    public static File createTempDirectoryInDefaultTempDirectory(final String directoryName) {
        final File tmpDir = new File(TMP_DIRECTORY, directoryName + "_" + JVM_NAME + "_" + System.currentTimeMillis());
        createTempDirectory(tmpDir);
        return tmpDir;
    }

    public static File createTempDirectory(final URI directoryPath) {
        final File tmpDir = new File(directoryPath);
        createTempDirectory(tmpDir);
        return tmpDir;
    }

    private static void createTempDirectory(final File tmpDir) {
        tmpDir.setReadable(true);
        tmpDir.setWritable(true);

        mkdirs(tmpDir);

        Runtime.getRuntime().addShutdownHook(new Thread() {

            @Override
            public void run() {
                try {
                    final boolean deleted = deleteDir(tmpDir);
                    if (!deleted) {
                        System.err.println("Unable to delete the directory: " + tmpDir);
                    }
                } catch (final IOException e) {
                    throw new BonitaRuntimeException(e);
                }
            }
        });
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

    public static File createTempFileInDefaultTempDirectory(final String prefix, final String suffix) throws IOException {
        return createTempFile(prefix, suffix, new File(TMP_DIRECTORY));
    }

    public static File createTempFile(final String prefix, final String suffix, final File directory) throws IOException {
        final File tmpFile = createTempFileUntilSuccess(prefix, suffix, directory);

        Runtime.getRuntime().addShutdownHook(new Thread() {

            @Override
            public void run() {
                if (tmpFile != null) {
                    deleteFile(tmpFile, 1, 0);
                }
            }
        });
        return tmpFile;
    }

    private static File createTempFileUntilSuccess(final String prefix, final String suffix, final File directory) throws IOException {
        // By-pass for the bug #6325169 on SUN JDK 1.5 on windows
        // The createTempFile could fail while creating a file with the same name of
        // an existing directory
        // So if the file creation fail, it retry (with a limit of 10 retry)
        // Rethrow the IOException if all retries failed
        File tmpFile = null;
        final int retryNumber = 10;
        int j = 0;
        boolean succeded = false;
        do {
            try {
                /*
                 * If the prefix contained file separator
                 * we need to create the parent directories if missing
                 */
                final int lastIndexOfSeparatorChar = prefix.lastIndexOf('/');
                String fileName = prefix;
                if (lastIndexOfSeparatorChar > -1) {
                    final String dirToCreate = prefix.substring(0, lastIndexOfSeparatorChar);
                    new File(directory.getAbsolutePath() + File.separator + dirToCreate).mkdirs();
                    fileName = prefix.substring(lastIndexOfSeparatorChar, prefix.length());
                }

                /* Create the file */
                tmpFile = File.createTempFile(fileName, suffix, directory);

                succeded = true;
            } catch (final IOException e) {
                if (j == retryNumber) {
                    throw e;
                }
                try {
                    Thread.sleep(100);
                } catch (final InterruptedException e1) {
                }
                j++;
            }
        } while (!succeded);
        return tmpFile;
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
            } catch (final InterruptedException e) {
            }
        }
        return retries > 0;
    }

    public static byte[] zip(final Map<String, byte[]> files) throws IOException {
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        final ZipOutputStream zos = new ZipOutputStream(baos);
        try {
            for (final Entry<String, byte[]> file : files.entrySet()) {
                zos.putNextEntry(new ZipEntry(file.getKey()));
                zos.write(file.getValue());
                zos.closeEntry();
            }
            return baos.toByteArray();
        } finally {
            zos.close();
        }
    }

    /**
     * Create a structured zip archive recursively.
     * The string must be OS specific String to represent path.
     *
     * @param dir2zip
     * @param zos
     * @param root
     * @throws IOException
     */
    public static void zipDir(final String dir2zip, final ZipOutputStream zos, final String root) throws IOException {
        final File zipDir = new File(dir2zip);
        final byte[] readBuffer = new byte[BUFFER_SIZE];
        int bytesIn = 0;

        for (final String pathName : zipDir.list()) {
            final File file = new File(zipDir, pathName);
            final String path = file.getPath();
            if (file.isDirectory()) {
                zipDir(path, zos, root);
                continue;
            }

            try {
                final ZipEntry anEntry = new ZipEntry(path.substring(root.length() + 1, path.length()).replace(String.valueOf(File.separatorChar), "/"));
                zos.putNextEntry(anEntry);
                bytesIn = +copyFileToZip(zos, readBuffer, file, bytesIn);
                zos.flush();
            } finally {
                zos.closeEntry();
            }
        }
    }

    private static int copyFileToZip(final ZipOutputStream zos, final byte[] readBuffer, final File file, final int bytesInOfZip) throws FileNotFoundException,
            IOException {
        final FileInputStream fis = new FileInputStream(file);
        int bytesIn = bytesInOfZip;
        try {
            while ((bytesIn = fis.read(readBuffer)) != -1) {
                zos.write(readBuffer, 0, bytesIn);
            }
        } finally {
            fis.close();
        }
        return bytesIn;
    }

    /**
     * Read the contents from the given FileInputStream. Return the result as a String.
     *
     * @param inputStream
     *        the stream to read from
     * @return the content read from the inputStream, as a String
     */
    public static String read(final InputStream inputStream) {
        if (inputStream == null) {
            throw new IllegalArgumentException("Input stream is null");
        }
        Scanner scanner = null;
        try {
            scanner = new Scanner(inputStream, FILE_ENCODING);
            return read(scanner);
        } finally {
            if (scanner != null) {
                scanner.close();
            }
        }
    }

    private static String read(final Scanner scanner) {
        final StringBuilder text = new StringBuilder();
        boolean isFirst = true;
        while (scanner.hasNextLine()) {
            if (isFirst) {
                text.append(scanner.nextLine());
            } else {
                text.append(LINE_SEPARATOR + scanner.nextLine());
            }
            isFirst = false;
        }
        return text.toString();
    }

    /**
     * Read the contents of the given file.
     * 
     * @param file
     */
    public static String read(final File file) throws IOException {
        FileInputStream fileInputStream = null;
        try {
            fileInputStream = new FileInputStream(file);
            return read(fileInputStream);
        } finally {
            if (fileInputStream != null) {
                fileInputStream.close();
            }
        }
    }

    public static void unzipToFolder(final InputStream inputStream, final File outputFolder) throws IOException {
        final ZipInputStream zipInputstream = new ZipInputStream(inputStream);

        try {
            extractZipEntries(zipInputstream, outputFolder);
        } finally {
            zipInputstream.closeEntry();
            zipInputstream.close();
        }
    }

    private static boolean mkdirs(final File file) {
        if (!file.exists()) {
            return file.mkdirs();
        }
        return true;
    }

    private static void extractZipEntries(final ZipInputStream zipInputstream, final File outputFolder) throws FileNotFoundException,
            IOException {
        ZipEntry zipEntry = null;
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

    private static void writeZipInputToFile(final ZipInputStream zipInputstream, final File outputFile) throws FileNotFoundException, IOException {
        // The input is a file. An FileOutputStream is created to write the content of the new file.
        mkdirs(outputFile.getParentFile());
        final FileOutputStream fileOutputStream = new FileOutputStream(outputFile);
        try {
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
        } finally {
            fileOutputStream.close();
        }
    }

    public static void writeContentToFile(final String content, final File outputFile) throws IOException {
        final FileOutputStream fileOutput = new FileOutputStream(outputFile);
        writeContentToFileOutputStream(content, fileOutput);
    }

    public static void writeContentToFileOutputStream(final String content, final FileOutputStream fileOutput) throws IOException {
        OutputStreamWriter out = null;
        try {
            out = new OutputStreamWriter(fileOutput, FILE_ENCODING);
            out.write(content);
            out.flush();
        } finally {
            if (out != null) {
                out.close();
            }
            fileOutput.close();
        }
    }

    public static void write(final File file, final byte[] fileContent) throws FileNotFoundException, IOException {
        FileOutputStream fos = null;
        BufferedOutputStream bos = null;
        try {
            fos = new FileOutputStream(file);
            bos = new BufferedOutputStream(fos);
            bos.write(fileContent);
            bos.flush();
        } finally {
            if (bos != null) {
                bos.close();
            }
            if (fos != null) {
                fos.close();
            }
        }
    }

    public static byte[] getContent(final File file) throws FileNotFoundException, IOException {
        FileChannel ch = null;
        FileInputStream fin = null;
        try {
            fin = new FileInputStream(file);
            ch = fin.getChannel();
            final int size = (int) ch.size();
            final MappedByteBuffer buf = ch.map(MapMode.READ_ONLY, 0, size);
            final byte[] bytes = new byte[size];
            buf.get(bytes);
            return bytes;
        } finally {
            if (ch != null) {
                ch.close();
            }
            if (fin != null) {
                fin.close();
            }
        }
    }

    public static byte[] marshallObjectToXML(final Object jaxbModel, final URL schemaURL) throws JAXBException, IOException, SAXException {
        if (jaxbModel == null) {
            return null;
        }
        if (schemaURL == null) {
            throw new IllegalArgumentException("schemaURL is null");
        }
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        final SchemaFactory sf = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        final Schema schema = sf.newSchema(schemaURL);
        try {
            final JAXBContext contextObj = JAXBContext.newInstance(jaxbModel.getClass());
            final Marshaller m = contextObj.createMarshaller();
            m.setSchema(schema);
            m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            m.marshal(jaxbModel, baos);
        } finally {
            baos.close();
        }
        return baos.toByteArray();
    }

    public static <T> T unmarshallXMLtoObject(final byte[] xmlObject, final Class<T> objectClass, final URL schemaURL) throws JAXBException, IOException,
            SAXException {
        if (xmlObject == null) {
            return null;
        }
        if (schemaURL == null) {
            throw new IllegalArgumentException("schemaURL is null");
        }
        final SchemaFactory sf = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        final Schema schema = sf.newSchema(schemaURL);
        final JAXBContext contextObj = JAXBContext.newInstance(objectClass);
        final Unmarshaller um = contextObj.createUnmarshaller();
        um.setSchema(schema);
        final ByteArrayInputStream bais = new ByteArrayInputStream(xmlObject);
        final StreamSource ss = new StreamSource(bais);
        try {
            final JAXBElement<T> jaxbElement = um.unmarshal(ss, objectClass);
            return jaxbElement.getValue();
        } finally {
            bais.close();
        }
    }

}
