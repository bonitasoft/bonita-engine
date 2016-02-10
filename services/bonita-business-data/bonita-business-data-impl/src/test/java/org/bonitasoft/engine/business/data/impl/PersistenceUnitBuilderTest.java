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
package org.bonitasoft.engine.business.data.impl;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashSet;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

/**
 * @author Romain Bioteau
 */
public class PersistenceUnitBuilderTest {

    private PersistenceUnitBuilder builder;

    /**
     * @throws Exception
     */
    @Before
    public void setUp() throws Exception {
        builder = new PersistenceUnitBuilder();
    }

    @Test
    public void shouldDone_WhenNothingIsCalledReturnsDefaultPersistenceUnits() {
        Document document = builder.done();
        assertThat(document).isNotNull();
        assertThat(document.getElementsByTagName("persistence-unit").getLength()).isEqualTo(1);
        assertThat(document.getElementsByTagName("class").getLength()).isEqualTo(0);
    }

    @Test
    public void shouldAddClass_AddOneClassTagByClassname() throws Exception {
        builder.addClass("org.bonitasoft.businessdata.Employee").addClass("org.bonitasoft.businessdata.LeaveRequesst");
        Document document = builder.done();
        assertThat(document.getElementsByTagName("class").getLength()).isEqualTo(2);
        NodeList nodeList = document.getElementsByTagName("class");
        Set<String> classnames = new HashSet<String>();
        for (int i = 0; i < nodeList.getLength(); i++) {
            classnames.add(nodeList.item(i).getTextContent());
        }
        assertThat(classnames).containsOnly("org.bonitasoft.businessdata.Employee", "org.bonitasoft.businessdata.LeaveRequesst");
    }

}
