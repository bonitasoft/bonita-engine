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
package org.bonitasoft.engine.bpm.contract.impl;

import static org.assertj.core.api.Assertions.assertThat;

import org.bonitasoft.engine.bpm.contract.ComplexInputDefinition;
import org.junit.Test;


public class ComplexInputDefinitionImplTest {

    private static final String DESCRIPTION = "description";
    private static final String NAME = "name";
    private ComplexInputDefinition complexInputDefinition;

    @Test
    public void constructor_multiple() throws Exception {
        complexInputDefinition = new ComplexInputDefinitionImpl(NAME, DESCRIPTION, true);

        //then
        assertThat(complexInputDefinition.isMultiple()).as("should be multiple").isTrue();
        assertThat(complexInputDefinition.getName()).as("should get name").isEqualTo(NAME);
        assertThat(complexInputDefinition.getDescription()).as("should get description").isEqualTo(DESCRIPTION);

    }

    @Test
    public void constructor_not_multiple() throws Exception {
        //given
        complexInputDefinition = new ComplexInputDefinitionImpl(NAME, DESCRIPTION, false);

        //then
        assertThat(complexInputDefinition.isMultiple()).as("should not be multiple").isFalse();
    }

    @Test
    public void toString_should_mention_multiple() throws Exception {
        //given
        complexInputDefinition = new ComplexInputDefinitionImpl(NAME, DESCRIPTION);

        //then
        assertThat(complexInputDefinition.toString()).containsIgnoringCase("multiple");
    }

}
