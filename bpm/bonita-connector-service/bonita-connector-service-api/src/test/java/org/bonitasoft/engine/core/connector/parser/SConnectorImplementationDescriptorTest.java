/*
 * Copyright (C) 2017 Bonitasoft S.A.
 * Bonitasoft, 32 rue Gustave Eiffel - 38000 Grenoble
 * This library is free software; you can redistribute it and/or modify it under the terms
 * of the GNU Lesser General Public License as published by the Free Software Foundation
 * version 2.1 of the License.
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License along with this
 * program; if not, write to the Free Software Foundation, Inc., 51 Franklin Street, Fifth
 * Floor, Boston, MA 02110-1301, USA.
 */

package org.bonitasoft.engine.core.connector.parser;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

/**
 * @author Danila Mazour
 */
public class SConnectorImplementationDescriptorTest {

    @Test
    public void theConstructorShouldNotGenerateANullFieldForJarsDependencies() {

        SConnectorImplementationDescriptor theConnector = new SConnectorImplementationDescriptor("implementationClassName", "id", "version", "definitionId",
                "definitionVersion", null);

        assertThat(theConnector.getJarDependencies()).isNotNull();
        assertThat(theConnector.getDefinitionId()).isEqualTo("definitionId");
        assertThat(theConnector.getImplementationClassName()).isEqualTo("implementationClassName");
        assertThat(theConnector.getVersion()).isEqualTo("version");
        assertThat(theConnector.getDefinitionVersion()).isEqualTo("definitionVersion");
        assertThat(theConnector.getId()).isEqualTo("id");
    }

    @Test
    public void theDefaultConstructorShouldNotGenerateANullFieldForJarsDependencies(){
        
        SConnectorImplementationDescriptor theConnector = new SConnectorImplementationDescriptor();
        assertThat(theConnector.getJarDependencies()).isNotNull();
        
    }
}
