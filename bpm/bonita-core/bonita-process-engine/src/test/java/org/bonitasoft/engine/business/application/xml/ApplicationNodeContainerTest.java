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
package org.bonitasoft.engine.business.application.xml;


import static org.bonitasoft.engine.business.application.xml.ApplicationNodeContainerAssert.assertThat;

import org.junit.Test;

public class ApplicationNodeContainerTest {

    @Test
    public void getApplications_should_return_empty_list_when_has_no_elements() throws Exception {
        //given
        ApplicationNodeContainer container = new ApplicationNodeContainer();

        //then
        assertThat(container).hasNoApplications();
    }

    @Test
    public void addApplication_should_add_new_entry_to_application_list() throws Exception {
        //given
        ApplicationNode app1 = new ApplicationNode();
        app1.setToken("app1");

        ApplicationNode app2 = new ApplicationNode();
        app2.setToken("app2");

        ApplicationNodeContainer container = new ApplicationNodeContainer();

        //when
        container.addApplication(app1);
        container.addApplication(app2);

        //then
        assertThat(container).hasApplications(app1, app2);

    }
}
