/**
 * Copyright (C) 2026 Bonitasoft S.A.
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
package org.bonitasoft.engine.bpm.data;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import org.bonitasoft.engine.bpm.data.impl.ShortTextDataInstanceImpl;
import org.junit.jupiter.api.Test;

/**
 * BPA-833: <code>isTransientData()</code> returns the wrapper <code>Boolean</code>, and the JavaBeans
 * specification only sanctions the <code>is</code> accessor for a <em>primitive</em> <code>boolean</code>.
 * Groovy 4 enforces that, so without the <code>getTransientData()</code> alias the
 * <code>dataInstance.transientData</code> shorthand used in process expressions has no usable getter and falls
 * back to reading the private field - which Groovy 4 refuses to do across a class hierarchy. Every instance a
 * script receives is a concrete subclass of <code>DataInstanceImpl</code>, never the declaring class, so the
 * shorthand breaks.
 * <p>
 * That the property is readable at all is enforced repository-wide by {@code BooleanAccessorPairingTest}, and the
 * end-to-end Groovy behaviour on a proxied receiver by
 * {@code DataInstanceIntegrationLocalIT#should_read_transientData_of_a_process_data_instance_as_a_groovy_property}.
 * What is left here is the value contract: the alias must report the same thing the accessor it delegates to would.
 */
class DataInstanceTest {

    @Test
    void getTransientData_should_report_the_configured_transience() {
        //given
        final DataDefinition transientDefinition = mock(DataDefinition.class);
        doReturn(true).when(transientDefinition).isTransientData();
        final DataDefinition persistedDefinition = mock(DataDefinition.class);
        doReturn(false).when(persistedDefinition).isTransientData();

        //when-then both branches, since the alias must agree with the accessor for either value
        assertThat(new ShortTextDataInstanceImpl(transientDefinition).getTransientData()).isTrue();
        assertThat(new ShortTextDataInstanceImpl(persistedDefinition).getTransientData()).isFalse();
    }

}
