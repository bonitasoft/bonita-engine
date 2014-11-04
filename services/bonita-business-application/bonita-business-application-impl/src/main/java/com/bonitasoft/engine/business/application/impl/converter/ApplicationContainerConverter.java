/*******************************************************************************
 * Copyright (C) 2014 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 ******************************************************************************/

package com.bonitasoft.engine.business.application.impl.converter;

import java.util.List;

import com.bonitasoft.engine.business.application.SBonitaExportException;
import com.bonitasoft.engine.business.application.model.SApplication;
import com.bonitasoft.engine.business.application.model.xml.ApplicationNodeContainer;

/**
 * @author Elias Ricken de Medeiros
 */
public class ApplicationContainerConverter {

    private ApplicationNodeConverter applicationNodeConverter;

    public ApplicationContainerConverter(ApplicationNodeConverter applicationNodeConverter) {
        this.applicationNodeConverter = applicationNodeConverter;
    }

    public ApplicationNodeContainer toNode(List<SApplication> applications) throws SBonitaExportException {
        ApplicationNodeContainer container = new ApplicationNodeContainer();
        for (SApplication application : applications) {
            container.addApplication(applicationNodeConverter.toNode(application));
        }
        return container;
    }

}
