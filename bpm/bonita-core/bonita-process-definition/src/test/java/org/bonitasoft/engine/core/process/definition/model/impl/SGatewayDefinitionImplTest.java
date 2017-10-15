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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.bonitasoft.engine.core.process.definition.model.SGatewayType;
import org.junit.Test;

/**
 * @author Elias Ricken de Medeiros
 * @author Celine Souchet
 * 
 */
public class SGatewayDefinitionImplTest {

    @Test
    public void not_exclusive_if_parallel_gateway() {
        final SGatewayDefinitionImpl gateway = new SGatewayDefinitionImpl(6, "name", SGatewayType.PARALLEL);

        assertFalse(gateway.isExclusive());
    }

    @Test
    public void not_exclusive_if_inclusive_gateway() {
        final SGatewayDefinitionImpl gateway = new SGatewayDefinitionImpl(6, "name", SGatewayType.INCLUSIVE);

        assertFalse(gateway.isExclusive());
    }

    @Test
    public void exclusive_if_exclusive_gateway() {
        final SGatewayDefinitionImpl gateway = new SGatewayDefinitionImpl(6, "name", SGatewayType.EXCLUSIVE);

        assertTrue(gateway.isExclusive());
    }

    @Test
    public void parallelOrInclusive_if_parallel_gateway() {
        final SGatewayDefinitionImpl gateway = new SGatewayDefinitionImpl(6, "name", SGatewayType.PARALLEL);

        assertTrue(gateway.isParalleleOrInclusive());
    }

    @Test
    public void parallelOrInclusive_if_inclusive_gateway() {
        final SGatewayDefinitionImpl gateway = new SGatewayDefinitionImpl(6, "name", SGatewayType.PARALLEL);

        assertTrue(gateway.isParalleleOrInclusive());
    }

    @Test
    public void not_parallelOrInclusive_if_exclusive_gateway() {
        final SGatewayDefinitionImpl gateway = new SGatewayDefinitionImpl(6, "name", SGatewayType.EXCLUSIVE);

        assertFalse(gateway.isParalleleOrInclusive());
    }

}
