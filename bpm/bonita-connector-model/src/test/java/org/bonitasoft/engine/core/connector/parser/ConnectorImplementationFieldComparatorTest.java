/**
 * Copyright (C) 2023 Bonitasoft S.A.
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
 **/
package org.bonitasoft.engine.core.connector.parser;

import static org.assertj.core.api.Assertions.assertThat;
import static org.bonitasoft.engine.core.connector.parser.SConnectorImplementationDescriptor.*;

import java.util.Collections;

import org.junit.Test;

public class ConnectorImplementationFieldComparatorTest {

    @Test
    public void assertIsEqualByFieldComparator() {
        var connector1 = new SConnectorImplementationDescriptor("implementationClassName", "id", "version",
                "definitionId", "definitionVersion", Collections.emptyList());
        var connector2 = new SConnectorImplementationDescriptor("implementationClassName", "id", "version",
                "definitionId", "definitionVersion", Collections.emptyList());

        assertThat(connector1)
                .usingComparator(new ConnectorImplementationFieldComparator(null))
                .isEqualTo(connector2);

        assertThat(connector1)
                .usingComparator(new ConnectorImplementationFieldComparator(IMPLEMENTATION_ID))
                .isEqualTo(connector2);

        assertThat(connector1)
                .usingComparator(new ConnectorImplementationFieldComparator(IMPLEMENTATION_VERSION))
                .isEqualTo(connector2);

        assertThat(connector1)
                .usingComparator(new ConnectorImplementationFieldComparator(IMPLEMENTATION_CLASS_NAME))
                .isEqualTo(connector2);

        assertThat(connector1)
                .usingComparator(new ConnectorImplementationFieldComparator(DEFINITION_ID))
                .isEqualTo(connector2);

        assertThat(connector1)
                .usingComparator(new ConnectorImplementationFieldComparator(DEFINITION_VERSION))
                .isEqualTo(connector2);
    }

    @Test
    public void assertIsNotEqualByFieldComparator() {
        var connector1 = new SConnectorImplementationDescriptor();
        var connector2 = new SConnectorImplementationDescriptor("implementationClassName", "id", "version",
                "definitionId", "definitionVersion", Collections.emptyList());

        assertThat(connector1)
                .usingComparator(new ConnectorImplementationFieldComparator(null))
                .isNotEqualTo(connector2);

        assertThat(connector1)
                .usingComparator(new ConnectorImplementationFieldComparator(IMPLEMENTATION_ID))
                .isNotEqualTo(connector2);

        assertThat(connector1)
                .usingComparator(new ConnectorImplementationFieldComparator(IMPLEMENTATION_VERSION))
                .isNotEqualTo(connector2);

        assertThat(connector1)
                .usingComparator(new ConnectorImplementationFieldComparator(IMPLEMENTATION_CLASS_NAME))
                .isNotEqualTo(connector2);

        assertThat(connector1)
                .usingComparator(new ConnectorImplementationFieldComparator(DEFINITION_ID))
                .isNotEqualTo(connector2);

        assertThat(connector1)
                .usingComparator(new ConnectorImplementationFieldComparator(DEFINITION_VERSION))
                .isNotEqualTo(connector2);
    }

}
