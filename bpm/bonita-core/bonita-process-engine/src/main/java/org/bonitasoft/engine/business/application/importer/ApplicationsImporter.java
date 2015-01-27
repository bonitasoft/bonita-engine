/*******************************************************************************
 * Copyright (C) 2014 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 ******************************************************************************/

package org.bonitasoft.engine.business.application.importer;

import java.util.ArrayList;
import java.util.List;

import org.bonitasoft.engine.api.ImportStatus;
import org.bonitasoft.engine.business.application.xml.ApplicationNode;
import org.bonitasoft.engine.business.application.xml.ApplicationNodeContainer;
import org.bonitasoft.engine.exception.AlreadyExistsException;
import org.bonitasoft.engine.exception.ImportException;

/**
 * @author Elias Ricken de Medeiros
 */
public class ApplicationsImporter {

    private final ApplicationContainerImporter containerImporter;
    private final ApplicationImporter applicationImporter;

    public ApplicationsImporter(ApplicationContainerImporter containerImporter, ApplicationImporter applicationImporter) {
        this.containerImporter = containerImporter;
        this.applicationImporter = applicationImporter;
    }

    public List<ImportStatus> importApplications(final byte[] xmlContent, long createdBy) throws ImportException, AlreadyExistsException {
        ApplicationNodeContainer applicationNodeContainer = containerImporter.importXML(xmlContent);
        ArrayList<ImportStatus> importStatus = new ArrayList<ImportStatus>(applicationNodeContainer.getApplications().size());
        for (ApplicationNode applicationNode : applicationNodeContainer.getApplications()) {
            importStatus.add(applicationImporter.importApplication(applicationNode, createdBy));
        }
        return importStatus;
    }

}
