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
package org.bonitasoft.engine.data.definition.model;

import static org.assertj.core.api.Assertions.assertThat;

import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.util.Arrays;
import java.util.Optional;

import org.bonitasoft.engine.data.definition.model.impl.SDataDefinitionImpl;
import org.junit.jupiter.api.Test;

/**
 * BPA-833: <code>transientData</code> must be readable as a JavaBean property, because that is what Groovy
 * resolves the <code>definition.transientData</code> shorthand against. A wrapper-<code>Boolean</code>
 * <code>isTransientData()</code> is not a property getter under Groovy 4, so the accessor is declared primitive -
 * the backing field always was one and no implementation can return <code>null</code>.
 */
class SDataDefinitionTest {

    @Test
    void transientData_should_be_a_readable_bean_property() throws Exception {
        //given
        final Class<?> implementation = SDataDefinitionImpl.class;

        //when
        final Optional<PropertyDescriptor> transientData = Arrays
                .stream(Introspector.getBeanInfo(implementation).getPropertyDescriptors())
                .filter(descriptor -> "transientData".equals(descriptor.getName()))
                .findFirst();

        //then
        assertThat(transientData).isPresent();
        assertThat(transientData.get().getReadMethod()).isNotNull();
        // the return type IS pinned here, unlike for the public DataInstance / MaintenanceDetails, whose contract
        // is a wrapper and whose readability is enforced by BooleanAccessorPairingTest instead: for these internal
        // types the primitive is the fix, so a silent revert to the wrapper has to fail
        assertThat(transientData.get().getReadMethod().getReturnType()).isEqualTo(boolean.class);
    }

    @Test
    void isTransientData_should_return_the_configured_value() {
        //given
        final SDataDefinitionImpl definition = new SDataDefinitionImpl();
        definition.setTransientData(true);

        //when-then
        assertThat(definition.isTransientData()).isTrue();
    }

}
