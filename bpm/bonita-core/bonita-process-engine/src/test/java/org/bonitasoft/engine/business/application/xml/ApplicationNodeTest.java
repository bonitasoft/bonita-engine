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

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.Test;

public class ApplicationNodeTest {

    @Test
    public void getApplicationPages_should_return_empty_list_when_no_elements_were_added() throws Exception {
        //given
        ApplicationNode applicationNode = new ApplicationNode();

        //when
        List<ApplicationPageNode> applicationPages = applicationNode.getApplicationPages();

        //then
        assertThat(applicationPages).isEmpty();
    }

    @Test
    public void getApplicationMenus_should_return_empty_list_when_no_elements_were_added() throws Exception {
        //given
        ApplicationNode applicationNode = new ApplicationNode();

        //when
        List<ApplicationMenuNode> applicationMenus = applicationNode.getApplicationMenus();

        //then
        assertThat(applicationMenus).isEmpty();
    }
}
