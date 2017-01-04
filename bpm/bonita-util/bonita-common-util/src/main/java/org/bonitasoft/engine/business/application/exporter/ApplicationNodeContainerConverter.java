/**
 * Copyright (C) 2016 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
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
package org.bonitasoft.engine.business.application.exporter;

import java.io.IOException;

import javax.xml.bind.JAXBException;

import org.bonitasoft.engine.business.application.xml.ApplicationNodeContainer;
import org.bonitasoft.engine.io.IOUtils;
import org.xml.sax.SAXException;

public class ApplicationNodeContainerConverter {

    private static final String APPLICATION_XSD = "/application.xsd";

    public byte[] marshallToXML(final ApplicationNodeContainer applicationNodeContainer)
            throws JAXBException, IOException, SAXException {
        return IOUtils.marshallObjectToXML(applicationNodeContainer,
                ApplicationNodeContainer.class.getResource(APPLICATION_XSD));
    }

    public ApplicationNodeContainer unmarshallFromXML(final byte[] applicationXML)
            throws JAXBException, IOException, SAXException {
        return IOUtils.unmarshallXMLtoObject(applicationXML, ApplicationNodeContainer.class,
                ApplicationNodeContainer.class.getResource(APPLICATION_XSD));
    }

}
