/*******************************************************************************
 * Copyright (C) 2014 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 ******************************************************************************/

package org.bonitasoft.engine.business.application.importer;

import java.net.URL;

import org.bonitasoft.engine.business.application.xml.ApplicationNodeContainer;
import org.bonitasoft.engine.commons.io.IOUtil;
import org.bonitasoft.engine.exception.ImportException;

/**
 * @author Elias Ricken de Medeiros
 */
public class ApplicationContainerImporter {

    public ApplicationNodeContainer importXML(final byte[] xmlContent) throws ImportException {
        final URL resource = ApplicationNodeContainer.class.getResource("/applications.xsd");
        try {
            return IOUtil.unmarshallXMLtoObject(xmlContent, ApplicationNodeContainer.class, resource);
        } catch (final Exception e) {
            throw new ImportException(e);
        }
    }

}
