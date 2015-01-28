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

import java.util.List;

import org.bonitasoft.engine.business.application.converter.ApplicationContainerConverter;
import org.bonitasoft.engine.business.application.model.SApplication;
import org.bonitasoft.engine.business.application.xml.ApplicationNodeContainer;
import org.bonitasoft.engine.exception.ExportException;

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

    public byte[] export(List<SApplication> applications) throws ExportException {
        ApplicationNodeContainer container = converter.toNode(applications);
        return exporter.export(container);
    }

}
