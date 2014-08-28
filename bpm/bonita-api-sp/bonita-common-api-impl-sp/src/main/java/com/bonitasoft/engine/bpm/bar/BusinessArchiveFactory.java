/*******************************************************************************
 * Copyright (C) 2009, 2013 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.bpm.bar;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipOutputStream;

import org.bonitasoft.engine.bpm.bar.ActorMappingContribution;
import org.bonitasoft.engine.bpm.bar.BusinessArchive;
import org.bonitasoft.engine.bpm.bar.BusinessArchiveContribution;
import org.bonitasoft.engine.bpm.bar.ClasspathContribution;
import org.bonitasoft.engine.bpm.bar.ConnectorContribution;
import org.bonitasoft.engine.bpm.bar.DocumentsResourcesContribution;
import org.bonitasoft.engine.bpm.bar.ExternalResourceContribution;
import org.bonitasoft.engine.bpm.bar.InvalidBusinessArchiveFormatException;
import org.bonitasoft.engine.bpm.bar.UserFilterContribution;
import org.bonitasoft.engine.io.IOUtil;

/**
 * @author Baptiste Mesta
 */
public class BusinessArchiveFactory {

    private static final List<BusinessArchiveContribution> contributions;

    static {
        contributions = new ArrayList<BusinessArchiveContribution>();
        contributions.add(new ProcessDefinitionBARContributionExt());
        contributions.add(new ParameterContribution());
        contributions.add(new ConnectorContribution());
        contributions.add(new ExternalResourceContribution());
        contributions.add(new ActorMappingContribution());
        contributions.add(new UserFilterContribution());
        contributions.add(new DocumentsResourcesContribution());
        contributions.add(new ClasspathContribution());
    }

    public static BusinessArchive readBusinessArchive(final InputStream inputStream) throws IOException, InvalidBusinessArchiveFormatException {
        final File barFolder = IOUtil.createTempDirectoryInDefaultTempDirectory("tempBusinessArchive");
        IOUtil.unzipToFolder(inputStream, barFolder);

        final BusinessArchive businessArchive = new BusinessArchive();
        try {
            for (final BusinessArchiveContribution contribution : contributions) {
                if (!contribution.readFromBarFolder(businessArchive, barFolder) && contribution.isMandatory()) {
                    throw new InvalidBusinessArchiveFormatException("Invalid format, can't read '" + contribution.getName() + "' from the BAR file");
                }
            }
            return businessArchive;
        } catch (InvalidBusinessArchiveFormatException e) {
            throw e;
        } catch (Exception e) {
            throw new InvalidBusinessArchiveFormatException("Invalid format, can't read the BAR file", e);
        } finally {
            IOUtil.deleteDir(barFolder);
        }
    }

    /**
     * Create a business archive from a valid file or folder
     * 
     * @param barOrFolder
     * @return
     * @throws IOException
     * @throws InvalidBusinessArchiveFormatException
     */
    public static BusinessArchive readBusinessArchive(final File barOrFolder) throws InvalidBusinessArchiveFormatException, IOException {
        if (!barOrFolder.exists()) {
            throw new FileNotFoundException("the file does not exists: " + barOrFolder.getAbsolutePath());
        }
        if (barOrFolder.isDirectory()) {
            final BusinessArchive businessArchive = new BusinessArchive();
            for (final BusinessArchiveContribution contribution : contributions) {
                if (!contribution.readFromBarFolder(businessArchive, barOrFolder) && contribution.isMandatory()) {
                    throw new InvalidBusinessArchiveFormatException("Invalid format, can't read " + contribution.getName() + " from the BAR file");
                }
            }
            return businessArchive;
        }

        final FileInputStream inputStream = new FileInputStream(barOrFolder);
        try {
            return readBusinessArchive(inputStream);
        } finally {
            inputStream.close();
        }
    }

    public static void writeBusinessArchiveToFolder(final BusinessArchive businessArchive, final File folderPath) throws IOException {
        if (folderPath.exists()) {
            if (!folderPath.isDirectory()) {
                throw new IOException("unable to create Business archive on a file " + folderPath);
            }
        } else {
            folderPath.mkdir();
        }
        for (final BusinessArchiveContribution contribution : contributions) {
            contribution.saveToBarFolder(businessArchive, folderPath);
        }
    }

    public static void writeBusinessArchiveToFile(final BusinessArchive businessArchive, final File businessArchiveFile) throws IOException {
        // FIXME put it in tmp folder of the bonita home
        final File tempFile = IOUtil.createTempDirectoryInDefaultTempDirectory("tempBusinessArchiveFolder");
        writeBusinessArchiveToFolder(businessArchive, tempFile);
        zipBarFolder(businessArchiveFile, tempFile);
        IOUtil.deleteDir(tempFile);
    }

    public static String businessArchiveFolderToFile(final File destFile, final String folderPath) throws IOException {
        zipBarFolder(destFile, new File(folderPath));
        return destFile.getAbsolutePath();
    }

    private static void zipBarFolder(final File businessArchiveFile, final File folder) throws FileNotFoundException, IOException {
        // create a ZipOutputStream to zip the data to
        if (businessArchiveFile.exists()) {
            throw new IOException("The destination file already exists " + businessArchiveFile.getAbsolutePath());
        }
        final ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(businessArchiveFile));
        try {
            IOUtil.zipDir(folder.getAbsolutePath(), zos, folder.getAbsolutePath());
        } finally {
            zos.close();
        }
    }

}
