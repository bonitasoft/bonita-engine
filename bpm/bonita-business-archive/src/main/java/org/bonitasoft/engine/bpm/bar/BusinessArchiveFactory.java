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
package org.bonitasoft.engine.bpm.bar;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 * Read or write {@link BusinessArchive} from/to file system
 *
 * @author Baptiste Mesta
 */
public class BusinessArchiveFactory {

    private static final List<BusinessArchiveContribution> contributions;

    static {
        contributions = new ArrayList<>();
        contributions.add(new ProcessDefinitionBARContribution());
        contributions.add(new ParameterContribution());
        contributions.add(new ConnectorContribution());
        contributions.add(new ExternalResourceContribution());
        contributions.add(new ActorMappingContribution());
        contributions.add(new UserFilterContribution());
        contributions.add(new DocumentsResourcesContribution());
        contributions.add(new ClasspathContribution());
        contributions.add(new FormMappingContribution());
    }

    private static final BusinessArchiveFactory INSTANCE = new BusinessArchiveFactory();
    private static final int BUFFER_SIZE = 100000;

    /**
     * Create a business archive from an {@link InputStream}
     *
     * @param inputStream
     * @return
     *         the business archived that was in the input stream
     * @throws IOException
     *         in case of issue reading/writing on file system
     * @throws InvalidBusinessArchiveFormatException
     *         if the input stream does not contains a valid business archive
     */
    public static BusinessArchive readBusinessArchive(final InputStream inputStream)
            throws IOException, InvalidBusinessArchiveFormatException {
        return INSTANCE.readBusinessArchive(inputStream, contributions);
    }

    /**
     * Create a business archive from a valid file or folder
     *
     * @param barOrFolder
     *        the folder or file that contains the business archive to read
     * @return
     *         the business archived that was in the file or folder
     * @throws IOException
     *         in case of issue reading/writing on file system
     * @throws InvalidBusinessArchiveFormatException
     *         if the input stream does not contains a valid business archive
     */
    public static BusinessArchive readBusinessArchive(final File barOrFolder)
            throws InvalidBusinessArchiveFormatException, IOException {
        return INSTANCE.readBusinessArchive(barOrFolder, contributions);
    }

    /**
     * Write the {@link BusinessArchive} to the folder given in parameter
     * <p>
     * the written business archive the uncompressed version of
     * {@link #writeBusinessArchiveToFile(BusinessArchive, File)}
     *
     * @param businessArchive
     *        the {@link BusinessArchive} to write
     * @param folderPath
     *        the folder into the business archive must be written
     * @throws IOException
     */
    public static void writeBusinessArchiveToFolder(final BusinessArchive businessArchive, final File folderPath)
            throws IOException {
        INSTANCE.writeBusinessArchiveToFolder(businessArchive, folderPath, contributions);
    }

    /**
     * Write the {@link BusinessArchive} to the .bar file given in parameter.
     * <p>
     * this file can then be read using {@link #readBusinessArchive(File)}
     *
     * @param businessArchive
     *        the {@link BusinessArchive} to write
     * @param businessArchiveFile
     *        the folder into the business archive must be written
     */
    public static void writeBusinessArchiveToFile(final BusinessArchive businessArchive, final File businessArchiveFile)
            throws IOException {
        INSTANCE.writeBusinessArchiveToFile(businessArchive, businessArchiveFile, contributions);
    }

    /**
     * Save the uncompressed business archive folder to a compressed file.
     * <p>
     * this file can then be read using {@link #readBusinessArchive(File)}
     */
    public static String businessArchiveFolderToFile(final File destFile, final String folderPath) throws IOException {
        return INSTANCE.businessArchiveFolderToFile(destFile, folderPath, contributions);
    }

    //--------------- instance methods

    protected BusinessArchive readBusinessArchive(final InputStream inputStream,
            List<BusinessArchiveContribution> contributions)
            throws IOException, InvalidBusinessArchiveFormatException {
        File barFolder = Files.createTempDirectory("tempBarFolder").toFile();
        try {
            unzipToFolder(inputStream, barFolder);
            return getBusinessArchive(barFolder, contributions);
        } catch (final InvalidBusinessArchiveFormatException e) {
            throw e;
        } catch (final Exception e) {
            throw new InvalidBusinessArchiveFormatException("Invalid format, can't read the BAR file", e);
        } finally {
            deleteDir(barFolder.toPath());
        }
    }

    protected BusinessArchive readBusinessArchive(final File barOrFolder,
            List<BusinessArchiveContribution> contributions)
            throws InvalidBusinessArchiveFormatException, IOException {
        if (!barOrFolder.exists()) {
            throw new FileNotFoundException("the file does not exists: " + barOrFolder.getAbsolutePath());
        }
        if (barOrFolder.isDirectory()) {
            return getBusinessArchive(barOrFolder, contributions);
        }

        try (FileInputStream inputStream = new FileInputStream(barOrFolder)) {
            return readBusinessArchive(inputStream);
        }
    }

    private BusinessArchive getBusinessArchive(final File barFolder, List<BusinessArchiveContribution> contributions)
            throws IOException, InvalidBusinessArchiveFormatException {
        final BusinessArchive businessArchive = new BusinessArchive();
        for (final BusinessArchiveContribution contribution : contributions) {
            if (!contribution.readFromBarFolder(businessArchive, barFolder) && contribution.isMandatory()) {
                throw new InvalidBusinessArchiveFormatException(
                        "Invalid format, can't read '" + contribution.getName() + "' from the BAR file");
            }
        }
        return businessArchive;
    }

    protected void writeBusinessArchiveToFolder(final BusinessArchive businessArchive, final File folderPath,
            List<BusinessArchiveContribution> contributions)
            throws IOException {
        if (folderPath.exists()) {
            if (!folderPath.isDirectory()) {
                throw new IOException("unable to create Business archive on a file " + folderPath);
            }
        } else {
            folderPath.mkdirs();
        }
        for (final BusinessArchiveContribution contribution : contributions) {
            contribution.saveToBarFolder(businessArchive, folderPath);
        }
    }

    protected void writeBusinessArchiveToFile(final BusinessArchive businessArchive, final File businessArchiveFile,
            List<BusinessArchiveContribution> contributions) throws IOException {
        final File tempFile = Files.createTempDirectory("tempBarFolder").toFile();
        try {
            writeBusinessArchiveToFolder(businessArchive, tempFile);
            zipBarFolder(businessArchiveFile, tempFile);
        } finally {
            deleteDir(tempFile.toPath());
        }
    }

    private static void deleteDir(Path directory) throws IOException {
        try (Stream<Path> pathStream = Files.walk(directory)) {
            pathStream.sorted(Comparator.reverseOrder())
                    .map(Path::toFile)
                    .forEach(File::delete);
        }
    }

    protected String businessArchiveFolderToFile(final File destFile, final String folderPath,
            List<BusinessArchiveContribution> contributions)
            throws IOException {
        zipBarFolder(destFile, new File(folderPath));
        return destFile.getAbsolutePath();
    }

    private void zipBarFolder(final File businessArchiveFile, final File folder) throws IOException {
        // create a ZipOutputStream to zip the data to
        if (businessArchiveFile.exists()) {
            throw new IOException("The destination file already exists " + businessArchiveFile.getAbsolutePath());
        }

        final FileOutputStream fileOutput = new FileOutputStream(businessArchiveFile);
        try (ZipOutputStream zos = new ZipOutputStream(fileOutput)) {
            zipDir(folder.getAbsolutePath(), zos, folder.getAbsolutePath());
        } finally {
            fileOutput.close();
        }
    }

    private static void unzipToFolder(final InputStream inputStream, final File outputFolder) throws IOException {
        try (ZipInputStream zipInputstream = new ZipInputStream(inputStream)) {
            extractZipEntries(zipInputstream, outputFolder);
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
                    if (!outputFile.exists()) {
                        outputFile.mkdirs();
                    }
                    continue;
                }
                writeZipInputToFile(zipInputstream, outputFile);
            } finally {
                zipInputstream.closeEntry();
            }
        }
    }

    private static void writeZipInputToFile(final ZipInputStream zipInputstream, final File outputFile)
            throws IOException {
        // The input is a file. An FileOutputStream is created to write the content of the new file.
        if (!outputFile.getParentFile().exists()) {
            outputFile.getParentFile().mkdirs();
        }
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

    private static void zipDir(final String dir2zip, final ZipOutputStream zos, final String root) throws IOException {
        final File zipDir = new File(dir2zip);
        final byte[] readBuffer = new byte[BUFFER_SIZE];

        for (final String pathName : zipDir.list()) {
            final File file = new File(zipDir, pathName);
            final String path = file.getPath();
            if (file.isDirectory()) {
                zipDir(path, zos, root);
                continue;
            }
            try {
                final ZipEntry anEntry = new ZipEntry(path.substring(root.length() + 1, path.length())
                        .replace(String.valueOf(File.separatorChar), "/"));
                zos.putNextEntry(anEntry);
                copyFileToZip(zos, readBuffer, file);
                zos.flush();
            } finally {
                zos.closeEntry();
            }
        }
    }

    private static void copyFileToZip(final ZipOutputStream zos, final byte[] readBuffer, final File file)
            throws IOException {
        int bytesIn;
        try (var fis = Files.newInputStream(file.toPath())) {
            while ((bytesIn = fis.read(readBuffer)) != -1) {
                zos.write(readBuffer, 0, bytesIn);
            }
        }
    }

}
