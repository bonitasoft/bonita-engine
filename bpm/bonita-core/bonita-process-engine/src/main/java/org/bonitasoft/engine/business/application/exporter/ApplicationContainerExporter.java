/**
 * Copyright (C) 2015 BonitaSoft S.A.
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

import java.net.URL;

import org.bonitasoft.engine.business.application.xml.ApplicationNodeContainer;
import org.bonitasoft.engine.exception.ExportException;
import org.bonitasoft.engine.io.IOUtils;

/**
 * @author Elias Ricken de Medeiros
 */
public class ApplicationContainerExporter {

    public byte[] export(final ApplicationNodeContainer applicationNodeContainer) throws ExportException {
        final URL resource = ApplicationNodeContainer.class.getResource("/applications.xsd");
        try {
            return IOUtils.marshallObjectToXML(applicationNodeContainer, resource);
        } catch (final Exception e) {
            throw new ExportException(e);
        }
    }

}
