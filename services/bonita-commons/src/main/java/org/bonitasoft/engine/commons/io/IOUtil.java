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
package org.bonitasoft.engine.commons.io;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Writer;
import java.math.BigInteger;
import java.net.URI;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Scanner;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
import java.util.jar.JarOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.bonitasoft.engine.commons.ClassDataUtil;
import org.bonitasoft.engine.commons.NullCheckingUtil;
import org.bonitasoft.engine.commons.Pair;
import org.w3c.dom.Document;
import sun.misc.IOUtils;
import sun.security.util.ManifestDigester;

/**
 * @author Elias Ricken de Medeiros
 * @author Matthieu Chaffotte
 * @author Celine Souchet
 */
public class IOUtil {

    private static final String LINE_SEPARATOR = System.getProperty("line.separator");

    public static final String TMP_DIRECTORY = System.getProperty("java.io.tmpdir");

    private static final int BUFFER_SIZE = 100000;

    public static final String FILE_ENCODING = "UTF-8";

    private IOUtil() {
        // For Sonar
    }

    public static File createDirectory(final String path) {
        final File file = new File(path);
        if (!file.exists()) {
            file.mkdirs();
        }
        return file;
    }

    public static List<String> getClassNameList(final byte[] jarContent) throws IOException {
        final List<String> classes = new ArrayList<String>(10);
        JarInputStream stream = null;
        ByteArrayInputStream byteArrayInputStream = null;
        try {
            byteArrayInputStream = new ByteArrayInputStream(jarContent);
            stream = new JarInputStream(byteArrayInputStream);
            JarEntry nextJarEntry = null;
            while ((nextJarEntry = stream.getNextJarEntry()) != null) {
                final String name = nextJarEntry.getName();
                if (name.endsWith(".class")) {
                    classes.add(toQualifiedClassName(name));
                }
            }
        } finally {
            if (stream != null) {
                stream.close();
            }
            if (byteArrayInputStream != null) {
                byteArrayInputStream.close();
            }
        }
        return classes;
    }

    private static String toQualifiedClassName(final String name) {
        return name.replace('/', '.').replaceAll(".class", "");
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

    public static void write(final File file, final byte[] fileContent) throws IOException {
        NullCheckingUtil.checkArgsNotNull(file, fileContent);
        if (!file.exists()) {
            file.getParentFile().mkdirs();
            file.createNewFile();
        }
        final OutputStream os = new FileOutputStream(file);
        try {
            os.write(fileContent);
            os.flush();
        } finally {
            os.close();
        }
    }

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
            resources.put(clazz.getName().replace(".", "/") + ".class", ClassDataUtil.getClassData(clazz));
            for (final Class<?> internalClass : clazz.getDeclaredClasses()) {
                resources.put(internalClass.getName().replace(".", "/") + ".class", ClassDataUtil.getClassData(internalClass));
            }
        }
        return resources;
    }

    public static byte[] generateJar(final Map<String, byte[]> resources) throws IOException {
        if (resources == null || resources.size() == 0) {
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

    public static byte[] generateZip(final Map<String, byte[]> resources) throws IOException {
        if (resources == null || resources.size() == 0) {
            final String message = "No resources available";
            throw new IOException(message);
        }

        ByteArrayOutputStream baos = null;
        ZipOutputStream zipOutStream = null;
        BufferedOutputStream bufferedOutputStream = null;
        try {
            baos = new ByteArrayOutputStream();
            bufferedOutputStream = new BufferedOutputStream(baos);
            zipOutStream = new ZipOutputStream(bufferedOutputStream);
            for (final Map.Entry<String, byte[]> resource : resources.entrySet()) {
                zipOutStream.putNextEntry(new ZipEntry(resource.getKey()));
                zipOutStream.write(resource.getValue());
            }
            zipOutStream.flush();
            baos.flush();
        } finally {
            if (zipOutStream != null) {
                zipOutStream.close();
            }
            if (baos != null) {
                baos.close();
            }
            if (bufferedOutputStream != null) {
                bufferedOutputStream.close();
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
     * Read the contents from the given FileInputStream. Return the result as a String.
     *
     * @param inputStream
     *        the stream to read from
     * @return the content read from the inputStream, as a String
     */
    public static String read(final InputStream inputStream) {
        final Scanner scanner = new Scanner(inputStream, FILE_ENCODING);
        final StringBuilder text = new StringBuilder();
        try {
            boolean isFirst = true;
            while (scanner.hasNextLine()) {
                if (isFirst) {
                    text.append(scanner.nextLine());
                } else {
                    text.append(LINE_SEPARATOR + scanner.nextLine());
                }
                isFirst = false;
            }
        } finally {
            scanner.close();
        }
        return text.toString();
    }

    /**
     * Read the contents of the given file.
     *
     * @param file
     */
    public static String read(final File file) throws IOException {
        final FileInputStream inputStream = new FileInputStream(file);
        try {
            return read(inputStream);
        } finally {
            inputStream.close();
        }
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

    public static boolean deleteDir(final File dir) throws IOException {
        return deleteDir(dir, 1, 0);
    }

    public static boolean deleteDir(final File dir, final int attempts, final long sleepTime) throws IOException {
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

    public static boolean deleteFile(final File file, final int attempts, final long sleepTime) {
        int retries = attempts;
        while (retries > 0) {
            if (file.delete()) {
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

    public static String getFileContent(final File file) {
        final StringBuilder sb = new StringBuilder();
        try {
            final BufferedReader reader = new BufferedReader(new FileReader(file));
            try {
                String line = null;
                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                    sb.append(System.getProperty("line.separator"));
                }
            } finally {
                reader.close();
            }
        } catch (final IOException ex) {
            ex.printStackTrace();
        }

        return sb.toString();
    }

    public static void writeFile(final File file, final String fileContent) throws FileNotFoundException, IOException {
        if (file == null) {
            throw new IllegalArgumentException("File should not be null.");
        }
        if (!file.exists()) {
            throw new FileNotFoundException("File does not exist: " + file);
        }
        if (!file.isFile()) {
            throw new IllegalArgumentException("Should not be a directory: " + file);
        }
        if (!file.canWrite()) {
            throw new IllegalArgumentException("File cannot be written: " + file);
        }

        // use buffering
        final Writer output = new BufferedWriter(new FileWriter(file));
        try {
            output.write(fileContent);
        } finally {
            output.close();
        }
    }

    public static void copyFile(final ZipInputStream inputStream, final File target) throws IOException {
        final FileOutputStream fos = new FileOutputStream(target);
        int n;
        final int BUFF_SIZE = 1024;
        final byte[] buf = new byte[BUFF_SIZE];
        try {
            while ((n = inputStream.read(buf)) > 0) {
                fos.write(buf, 0, n);
            }
        } finally {
            fos.close();
        }
    }

    public static byte[] zip(final Map<String, byte[]> files) throws IOException {
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        final ZipOutputStream zos = new ZipOutputStream(baos);
        try {
            for (final Entry<String, byte[]> file : files.entrySet()) {
                zos.putNextEntry(new ZipEntry(file.getKey()));
                zos.write(file.getValue());
                zos.flush();
                zos.closeEntry();
            }
        } finally {
            zos.close();
            baos.close();
        }
        return baos.toByteArray();
    }

    public static byte[] zip(final Pair<String, byte[]>... files) throws IOException {
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        final ZipOutputStream zos = new ZipOutputStream(baos);
        try {
            for (final Entry<String, byte[]> file : files) {
                zos.putNextEntry(new ZipEntry(file.getKey()));
                zos.write(file.getValue());
                zos.flush();
                zos.closeEntry();
            }
        } finally {
            zos.close();
            baos.close();
        }
        return baos.toByteArray();
    }

    public static final Map<String, byte[]> unzip(final byte[] zipFile) throws IOException {
        final ByteArrayInputStream bais = new ByteArrayInputStream(zipFile);
        final ZipInputStream zipInputstream = new ZipInputStream(bais);
        ZipEntry zipEntry = null;
        final Map<String, byte[]> files = new HashMap<String, byte[]>();
        try {
            while ((zipEntry = zipInputstream.getNextEntry()) != null) {
                final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                int bytesRead;
                final byte[] buffer = new byte[BUFFER_SIZE];
                while ((bytesRead = zipInputstream.read(buffer)) > -1) {
                    byteArrayOutputStream.write(buffer, 0, bytesRead);
                }
                files.put(zipEntry.getName(), byteArrayOutputStream.toByteArray());
            }
        } finally {
            zipInputstream.close();
        }
        return files;
    }

    public static void unzipToFolder(final InputStream inputStream, final File outputFolder) throws IOException {
        final ZipInputStream zipInputstream = new ZipInputStream(inputStream);
        ZipEntry zipEntry = null;

        try {
            while ((zipEntry = zipInputstream.getNextEntry()) != null) {
                extractZipEntry(zipInputstream, zipEntry, outputFolder);
            }
        } finally {
            zipInputstream.close();
        }
    }

    private static void extractZipEntry(final ZipInputStream zipInputstream, final ZipEntry zipEntry, final File outputFolder) throws FileNotFoundException,
            IOException {
        try {
            final String entryName = zipEntry.getName();

            // For each entry, a file is created in the output directory "folder"
            final File outputFile = new File(outputFolder.getAbsolutePath(), entryName);

            // If the entry is a directory, it creates in the output folder, and we go to the next entry (return).
            if (zipEntry.isDirectory()) {
                mkdirs(outputFile);
                return;
            }
            writeZipInputToFile(zipInputstream, outputFile);
        } finally {
            zipInputstream.closeEntry();
        }
    }

    private static void writeZipInputToFile(final ZipInputStream zipInputstream, final File outputFile) throws FileNotFoundException, IOException {
        // The input is a file. An FileOutputStream is created to write the content of the new file.
        mkdirs(outputFile.getParentFile());

        try {
            final FileOutputStream fileOutputStream = new FileOutputStream(outputFile);
            try {
                // The contents of the new file, that is read from the ZipInputStream using a buffer (byte []), is written.
                int bytesRead;
                final byte[] buffer = new byte[BUFFER_SIZE];
                while ((bytesRead = zipInputstream.read(buffer)) > -1) {
                    fileOutputStream.write(buffer, 0, bytesRead);
                }
            } finally {
                fileOutputStream.close();
            }
        } catch (final IOException ioe) {
            // In case of error, the file is deleted
            outputFile.delete();
            throw ioe;
        }
    }

    private static boolean mkdirs(final File file) {
        if (!file.exists()) {
            return file.mkdirs();
        }
        return true;
    }

    public static File createTempDirectoryInDefaultTempDirectory(final String directoryName) {
        final File tmpDir = new File(TMP_DIRECTORY, directoryName + "_" + System.currentTimeMillis());
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
                    e.printStackTrace();
                }
            }
        });
    }

    public static byte[] getZipEntryContent(final String entryName, final InputStream inputStream) throws IOException {
        final ZipInputStream zipInputstream = new ZipInputStream(inputStream);
        ZipEntry zipEntry = null;
        try {
            while ((zipEntry = zipInputstream.getNextEntry()) != null) {
                if (!entryName.equals(zipEntry.getName())) {
                    continue;
                }
                return getBytes(zipInputstream);
            }
        } finally {
            zipInputstream.close();
        }
        throw new IOException("Entry " + entryName + " does not exists in the zip file");
    }

    public static byte[] getBytes(ZipInputStream zipInputstream) throws IOException {
        final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        try {
            int bytesRead;
            final byte[] buffer = new byte[BUFFER_SIZE];
            while ((bytesRead = zipInputstream.read(buffer)) > -1) {
                byteArrayOutputStream.write(buffer, 0, bytesRead);
            }
            return byteArrayOutputStream.toByteArray();
        } finally {
            byteArrayOutputStream.close();
        }
    }

    public static byte[] getZipEntryContent(final String entryName, final byte[] zipFile) throws IOException {
        return getZipEntryContent(entryName, new ByteArrayInputStream(zipFile));
    }

    public static byte[] toByteArray(final Document document) throws IOException, TransformerException {
        if (document == null) {
            throw new IllegalArgumentException("Document should not be null.");
        }
        final Transformer tf = TransformerFactory.newInstance().newTransformer();
        tf.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
        tf.setOutputProperty(OutputKeys.INDENT, "yes");
        ByteArrayOutputStream out = null;
        try {
            out = new ByteArrayOutputStream();
            tf.transform(new DOMSource(document), new StreamResult(out));
            return out.toByteArray();
        } finally {
            if (out != null) {
                out.close();
            }
        }
    }

    public static byte[] addJarEntry(final byte[] jarToUpdate, final String entryName, final byte[] entryContent) throws IOException {
        ByteArrayOutputStream out = null;
        ByteArrayInputStream bais = null;
        JarOutputStream jos = null;
        JarInputStream jis = null;
        final byte[] buffer = new byte[4096];
        try {
            bais = new ByteArrayInputStream(jarToUpdate);
            jis = new JarInputStream(bais);
            out = new ByteArrayOutputStream();
            jos = new JarOutputStream(out);
            JarEntry inEntry;
            while ((inEntry = (JarEntry) jis.getNextEntry()) != null) {
                if (!inEntry.getName().equals(entryName)) {
                    jos.putNextEntry(new JarEntry(inEntry));
                } else {
                    throw new IllegalArgumentException("Jar entry " + entryName + " already exists in jar to update");
                }
                int len;
                while ((len = jis.read(buffer)) > 0) {
                    jos.write(buffer, 0, len);
                }
                jos.flush();
            }
            final JarEntry entry = new JarEntry(entryName);
            jos.putNextEntry(entry);
            jos.write(entryContent);
            jos.closeEntry();
            jos.finish();
            out.flush();
            return out.toByteArray();
        } finally {
            if (jos != null) {
                jos.close();
            }
            if (out != null) {
                out.close();
            }
            if (jis != null) {
                jis.close();
            }
            if (bais != null) {
                bais.close();
            }
        }
    }

    public static byte[] getPropertyAsString(final Properties prop, final String comment) throws IOException {
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        prop.store(out, comment);
        return out.toByteArray();
    }

    public static String readResource(String fileName) throws IOException {
        final String xmlContent;
        final InputStream inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(fileName);
        if (inputStream == null) {
            return null;
        }
        try {
            xmlContent = read(inputStream);
        } finally {
            inputStream.close();
        }
        return xmlContent;
    }

    public static String md5(byte[] content) throws NoSuchAlgorithmException {
        MessageDigest md5 = MessageDigest.getInstance("MD5");
        return new BigInteger(1, md5.digest(content)).toString(16);
    }

    public static void writeMD5(File file, byte[] bytes) throws NoSuchAlgorithmException, IOException {
        write(file, md5(bytes).getBytes());

    }

    public static boolean checkMD5(File md5File, byte[] contentToCheck) throws NoSuchAlgorithmException {
        if(!md5File.exists()){
            return false;
        }
        try {
            return read(md5File).equals(md5(contentToCheck));
        } catch (IOException e) {
            return false;
        }
    }

    public static void updatePropertyValue(File propertiesFile, final Map<String, String> pairs) throws IOException {
        final BufferedReader br = new BufferedReader(new FileReader(propertiesFile));
        try {
            StringBuilder sb = new StringBuilder();
            String line = br.readLine();

            while (line != null) {
                String lineToWrite = line;
                if (!line.startsWith("#")) {
                    //it is not a comment
                    final int splitCharIndex = line.indexOf("=");
                    if (splitCharIndex >= 0) {
                        final String key = line.substring(0, splitCharIndex);
                        //this is a key-value pair
                        if (pairs.containsKey(key.trim())) {
                            String value = pairs.get(key.trim());
                            value = value.replace("\\", "\\\\");
                            lineToWrite = key + "=" + value;
                        }
                    }
                }
                sb.append(lineToWrite);
                sb.append(System.lineSeparator());
                line = br.readLine();
            }
            IOUtil.writeFile(propertiesFile, sb.toString());
        } finally {
            if (br != null) {
                br.close();
            }
        }
    }
}
