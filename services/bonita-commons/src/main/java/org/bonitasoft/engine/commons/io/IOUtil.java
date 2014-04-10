/**
 * Copyright (C) 2011-20123 BonitaSoft S.A.
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
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
import org.w3c.dom.Document;

/**
 * @author Elias Ricken de Medeiros
 * @author Matthieu Chaffotte
 */
public class IOUtil {

    private static final int BUFF_SIZE = 100000;

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
        try {
            stream = new JarInputStream(new ByteArrayInputStream(jarContent));
            JarEntry nextJarEntry = null;
            while ((nextJarEntry = stream.getNextJarEntry()) != null) {
                String name = nextJarEntry.getName();
                if (name.endsWith(".class")) {
                    classes.add(toQualifiedClassName(name));
                }
            }
        } finally {
            if (stream != null) {
                stream.close();
            }
        }
        return classes;
    }

    private static String toQualifiedClassName(final String name) {
        return name.replace('/', '.').replaceAll(".class", "");
    }

    public static File createTempFile(final String prefix, final String suffix, final File directory) throws IOException {
        // By-pass for the bug #6325169 on SUN JDK 1.5 on windows
        // The createTempFile could fail while creating a file with the same name of
        // an existing directory
        // So if the file creation fail, it retry (with a limit of 10 retry)
        // Rethrow the IOException if all retries failed
        File tmpDir = null;
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
                tmpDir = File.createTempFile(fileName, suffix, directory);
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
        return tmpDir;
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

        try {
            baos = new ByteArrayOutputStream();
            zipOutStream = new ZipOutputStream(new BufferedOutputStream(baos));
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
        }

        return baos.toByteArray();
    }

    /**
     * Return the whole underlying stream content into a single String.
     * Warning: the whole content of stream will be kept in memory!! Use with
     * care!
     * 
     * @param in
     *            the stream to read
     * @return the whole content of the stream in a single String.
     * @throws IOException
     *             if an I/O exception occurs
     */
    public static byte[] getAllContentFrom(final InputStream in) throws IOException {
        if (in == null) {
            throw new IOException("The InputStream is null!");
        }
        final byte[] buffer = new byte[BUFF_SIZE];
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
     *            the file to read
     * @return the whole content of the file in a single String.
     * @throws IOException
     *             If an I/O exception occurs
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
     *            the URL to read
     * @return the whole content of the stream in a single String.
     * @throws IOException
     *             if an I/O exception occurs
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
            return false;
        }
        if (!dir.isDirectory()) {
            throw new IOException("Unable to delete directory: " + dir + ", it is not a directory");
        }
        final File[] files = dir.listFiles();
        for (int i = 0; i < files.length; i++) {
            if (files[i].isDirectory()) {
                deleteDir(files[i], attempts, sleepTime);
            } else {
                result = result && deleteFile(files[i], attempts, sleepTime);
            }
        }
        result = result && deleteFile(dir, attempts, sleepTime);
        return result;
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

    //
    // public static String getFileContent(final byte[] textFileContent) {
    // final StringBuilder sb = new StringBuilder();
    // try {
    // final BufferedInputStream is = new BufferedInputStream(new ByteArrayInputStream(textFileContent));
    // try {
    // byte[] buff = new byte[256];
    // int read = 0;
    // while ((read = is.read(buff)) != 0) {
    // sb.append(buff);
    // }
    // } finally {
    // is.close();
    // }
    // } catch (final IOException ex) {
    // ex.printStackTrace();
    // }
    //
    // return sb.toString();
    // }

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

    public static byte[] toByteArray(Document document) throws IOException, TransformerException {
        if (document == null) {
            throw new IllegalArgumentException("Document should not be null.");
        }
        Transformer tf = TransformerFactory.newInstance().newTransformer();
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

    public static byte[] addJarEntry(byte[] jarToUpdate, String entryName, byte[] entryContent) throws IOException {
        ByteArrayOutputStream out = null;
        ByteArrayInputStream bais = null;
        JarOutputStream jos = null;
        JarInputStream jis = null;
        byte[] buffer = new byte[4096];
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
            JarEntry entry = new JarEntry(entryName);
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
        }
    }

}
