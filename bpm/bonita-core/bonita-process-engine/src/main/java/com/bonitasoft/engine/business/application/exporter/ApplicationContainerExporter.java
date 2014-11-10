/*******************************************************************************
 * Copyright (C) 2014 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 ******************************************************************************/

package com.bonitasoft.engine.business.application.exporter;

import java.net.URL;

import org.bonitasoft.engine.exception.ExportException;

import com.bonitasoft.engine.business.application.xml.ApplicationNodeContainer;
import com.bonitasoft.engine.io.IOUtils;

/**
 * @author Elias Ricken de Medeiros
 */
public class ApplicationContainerExporter {

    public byte[] export(ApplicationNodeContainer applicationNodeContainer) throws ExportException {
        final URL resource = ApplicationNodeContainer.class.getResource("/applications.xsd");
        try {
            return IOUtils.marshallObjectToXML(applicationNodeContainer, resource);
        } catch (Exception e) {
            throw new ExportException(e);
        }
    }

}
