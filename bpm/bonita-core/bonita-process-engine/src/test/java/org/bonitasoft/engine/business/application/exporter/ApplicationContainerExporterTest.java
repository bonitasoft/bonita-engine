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

import static org.assertj.core.api.Assertions.assertThat;

import org.bonitasoft.engine.business.application.xml.ApplicationNodeContainer;
import org.junit.Test;

public class ApplicationContainerExporterTest {

    @Test
    public void exportApplications_should_call_marshall() throws Exception {
        final ApplicationContainerExporter exporter = new ApplicationContainerExporter();

        //when
        final byte[] bytes = exporter.export(new ApplicationNodeContainer());

        //then
        assertThat(new String(bytes)).isEqualTo("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" +
                "<applications xmlns=\"http://documentation.bonitasoft.com/application-xml-schema/1.0\"/>\n");
    }
}
