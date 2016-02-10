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
package org.bonitasoft.engine.bpm;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;

import org.bonitasoft.engine.bpm.data.DataDefinition;
import org.bonitasoft.engine.bpm.data.impl.DataDefinitionImpl;
import org.junit.Test;

public class ObjectSeekerTest {

    @Test
    public void seekANamedElementObject() {
        final List<DataDefinition> dataDefinitions = new ArrayList<DataDefinition>();
        final DataDefinitionImpl dataDefinitionImpl = new DataDefinitionImpl("var1", null);
        dataDefinitions.add(dataDefinitionImpl);
        dataDefinitions.add(new DataDefinitionImpl("var2", null));

        final DataDefinition dataDefinition = ObjectSeeker.getNamedElement(dataDefinitions, "var1");

        assertThat(dataDefinition).isEqualTo(dataDefinitionImpl);
    }

    @Test
    public void seekANullObjectReturnsNull() {
        final List<DataDefinition> dataDefinitions = new ArrayList<DataDefinition>();
        dataDefinitions.add(new DataDefinitionImpl("var1", null));
        dataDefinitions.add(new DataDefinitionImpl("var2", null));

        final DataDefinition dataDefinition = ObjectSeeker.getNamedElement(dataDefinitions, null);

        assertThat(dataDefinition).isNull();
    }

    @Test
    public void seekAnUnknownObjectReturnsNull() {
        final List<DataDefinition> dataDefinitions = new ArrayList<DataDefinition>();
        dataDefinitions.add(new DataDefinitionImpl("var1", null));
        dataDefinitions.add(new DataDefinitionImpl("var2", null));

        final DataDefinition dataDefinition = ObjectSeeker.getNamedElement(dataDefinitions, "var3");

        assertThat(dataDefinition).isNull();
    }

    @Test
    public void seekAObjectInANullListReturnsNull() {
        final DataDefinition dataDefinition = ObjectSeeker.getNamedElement(null, "var1");

        assertThat(dataDefinition).isNull();
    }

    @Test
    public void seekAnObjectInAnEmptyListReturnNull() {
        final List<DataDefinition> dataDefinitions = new ArrayList<DataDefinition>();

        final DataDefinition dataDefinition = ObjectSeeker.getNamedElement(dataDefinitions, "var3");

        assertThat(dataDefinition).isNull();
    }
}
