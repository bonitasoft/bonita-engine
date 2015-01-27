/*******************************************************************************
 * Copyright (C) 2014 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 ******************************************************************************/

package org.bonitasoft.engine.business.application.converter;

import java.util.ArrayList;
import java.util.List;

import org.bonitasoft.engine.business.application.importer.ImportResult;
import org.bonitasoft.engine.business.application.model.SApplication;
import org.bonitasoft.engine.business.application.xml.ApplicationNode;
import org.bonitasoft.engine.business.application.xml.ApplicationNodeContainer;
import org.bonitasoft.engine.exception.ExecutionException;
import org.bonitasoft.engine.exception.ExportException;

/**
 * @author Elias Ricken de Medeiros
 */
public class ApplicationContainerConverter {

    private final ApplicationNodeConverter applicationNodeConverter;

    public ApplicationContainerConverter(final ApplicationNodeConverter applicationNodeConverter) {
        this.applicationNodeConverter = applicationNodeConverter;
    }

    public ApplicationNodeContainer toNode(final List<SApplication> applications) throws ExportException {
        final ApplicationNodeContainer container = new ApplicationNodeContainer();
        for (final SApplication application : applications) {
            container.addApplication(applicationNodeConverter.toNode(application));
        }
        return container;
    }

    public List<ImportResult> toSApplications(final ApplicationNodeContainer applicationContainer, final long createdBy) throws ExecutionException {
        final List<ImportResult> applications = new ArrayList<ImportResult>();
        for (final ApplicationNode applicationNode : applicationContainer.getApplications()) {
            applications.add(applicationNodeConverter.toSApplication(applicationNode, createdBy));
        }
        return applications;
    }

}
