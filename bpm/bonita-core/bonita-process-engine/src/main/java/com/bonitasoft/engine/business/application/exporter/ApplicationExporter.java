/*******************************************************************************
 * Copyright (C) 2014 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 ******************************************************************************/

package com.bonitasoft.engine.business.application.exporter;

import java.util.List;

import org.bonitasoft.engine.exception.ExecutionException;

import com.bonitasoft.engine.business.application.converter.ApplicationContainerConverter;
import com.bonitasoft.engine.business.application.model.SApplication;
import com.bonitasoft.engine.business.application.xml.ApplicationNodeContainer;

/**
 * @author Elias Ricken de Medeiros
 */
public class ApplicationExporter {

    private final ApplicationContainerConverter converter;
    private final ApplicationContainerExporter exporter;

    public ApplicationExporter(ApplicationContainerConverter converter, ApplicationContainerExporter exporter) {
        this.converter = converter;
        this.exporter = exporter;
    }

    public byte[] export(List<SApplication> applications) throws ExecutionException {
        ApplicationNodeContainer container = converter.toNode(applications);
        return exporter.export(container);
    }

}
