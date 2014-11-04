/*******************************************************************************
 * Copyright (C) 2014 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 ******************************************************************************/

package com.bonitasoft.engine.business.application.impl.exporter;

import java.util.List;

import com.bonitasoft.engine.business.application.SBonitaExportException;
import com.bonitasoft.engine.business.application.impl.converter.ApplicationContainerConverter;
import com.bonitasoft.engine.business.application.model.SApplication;
import com.bonitasoft.engine.business.application.model.xml.ApplicationNodeContainer;

/**
 * @author Elias Ricken de Medeiros
 */
public class ApplicationsExporter {

    private final ApplicationContainerConverter converter;
    private final ApplicationContainerExporter exporter;

    public ApplicationsExporter(ApplicationContainerConverter converter, ApplicationContainerExporter exporter) {
        this.converter = converter;
        this.exporter = exporter;
    }

    public byte[] export(List<SApplication> applications) throws SBonitaExportException {
        ApplicationNodeContainer container = converter.toNode(applications);
        return exporter.export(container);
    }

}
