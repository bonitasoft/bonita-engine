/*******************************************************************************
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.bpm.bar;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.bonitasoft.engine.bpm.bar.ActorMappingContribution;
import org.bonitasoft.engine.bpm.bar.BusinessArchive;
import org.bonitasoft.engine.bpm.bar.BusinessArchiveContribution;
import org.bonitasoft.engine.bpm.bar.ClasspathContribution;
import org.bonitasoft.engine.bpm.bar.ConnectorContribution;
import org.bonitasoft.engine.bpm.bar.DocumentsResourcesContribution;
import org.bonitasoft.engine.bpm.bar.ExternalResourceContribution;
import org.bonitasoft.engine.bpm.bar.FormMappingContribution;
import org.bonitasoft.engine.bpm.bar.InvalidBusinessArchiveFormatException;
import org.bonitasoft.engine.bpm.bar.ParameterContribution;
import org.bonitasoft.engine.bpm.bar.UserFilterContribution;

/**
 * Read or write {@link BusinessArchive} from/to file system
 *
 * @author Baptiste Mesta
 */
public class BusinessArchiveFactory extends org.bonitasoft.engine.bpm.bar.BusinessArchiveFactory {

    private static final List<BusinessArchiveContribution> contributions;

    static {
        contributions = new ArrayList<>();
        contributions.add(new ProcessDefinitionBARContributionExt());
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

    public static BusinessArchive readBusinessArchive(final InputStream inputStream) throws IOException, InvalidBusinessArchiveFormatException {
        return INSTANCE.readBusinessArchive(inputStream, contributions);
    }

    public static BusinessArchive readBusinessArchive(final File barOrFolder) throws InvalidBusinessArchiveFormatException, IOException {
        return INSTANCE.readBusinessArchive(barOrFolder, contributions);
    }

    public static void writeBusinessArchiveToFolder(final BusinessArchive businessArchive, final File folderPath) throws IOException {
        INSTANCE.writeBusinessArchiveToFolder(businessArchive, folderPath, contributions);
    }

    public static void writeBusinessArchiveToFile(final BusinessArchive businessArchive, final File businessArchiveFile) throws IOException {
        INSTANCE.writeBusinessArchiveToFile(businessArchive, businessArchiveFile, contributions);
    }

    public static String businessArchiveFolderToFile(final File destFile, final String folderPath) throws IOException {
        return INSTANCE.businessArchiveFolderToFile(destFile, folderPath, contributions);
    }

}
