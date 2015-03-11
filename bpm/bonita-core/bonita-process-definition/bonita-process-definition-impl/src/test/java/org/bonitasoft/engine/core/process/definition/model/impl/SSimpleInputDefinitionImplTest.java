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
package org.bonitasoft.engine.core.process.definition.model.impl;

import static org.assertj.core.api.Assertions.assertThat;

import org.bonitasoft.engine.bpm.contract.SimpleInputDefinition;
import org.bonitasoft.engine.bpm.contract.Type;
import org.bonitasoft.engine.bpm.contract.impl.SimpleInputDefinitionImpl;
import org.bonitasoft.engine.core.process.definition.model.SType;
import org.junit.Test;

public class SSimpleInputDefinitionImplTest {

    private static final String NAME = "name";
    private static final String DESCRIPTION = "description";

    @Test
    public void contructor_with_name() throws Exception {
        //given
        final SSimpleInputDefinitionImpl sSimpleInputDefinitionImpl = new SSimpleInputDefinitionImpl(NAME, SType.TEXT, DESCRIPTION);

        //then
        assertThat(sSimpleInputDefinitionImpl.isMultiple()).isFalse();
    }

    @Test
    public void constructor_with_input_definition() throws Exception {
        //given
        final SimpleInputDefinition simpleInput = new SimpleInputDefinitionImpl(NAME, Type.TEXT, DESCRIPTION, true);

        //when
        final SSimpleInputDefinitionImpl sSimpleInputDefinitionImpl = new SSimpleInputDefinitionImpl(simpleInput);

        //then
        assertThat(sSimpleInputDefinitionImpl.isMultiple()).isTrue();
        assertThat(sSimpleInputDefinitionImpl.getName()).isEqualTo(NAME);
        assertThat(sSimpleInputDefinitionImpl.getDescription()).isEqualTo(DESCRIPTION);
        assertThat(sSimpleInputDefinitionImpl.getType()).isEqualTo(SType.TEXT);

    }

}
