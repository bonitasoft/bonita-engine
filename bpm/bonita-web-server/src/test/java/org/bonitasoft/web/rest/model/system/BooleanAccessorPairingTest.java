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
package org.bonitasoft.web.rest.model.system;

import java.util.List;

import org.bonitasoft.engine.bpm.BooleanAccessorPairingScan;
import org.junit.jupiter.api.Test;

/**
 * BPA-833: runs the accessor-pairing guard across the Community product.
 * <p>
 * This module hosts it because it has the widest classpath of the edition - the whole engine plus the web layer, 4175
 * classes measured, against 3417 for the {@code bonita-server} fat jar. The guard used to live in {@code
 * bonita-common} and saw 868, which left {@code SDataInstance} and {@code SDataDefinition} - two of the four types
 * BPA-833 fixed - outside it, and {@code MaintenanceDetailsClient} in this very module, a third, outside it as well.
 * A scan scoped too narrowly and reporting clean is how BPA-833 reached a release, so the module with the most to
 * scan is where this belongs.
 * <p>
 * Coverage still stops short of {@code bonita-client}, {@code bonita-web-extensions-sp}, {@code platform-setup} and
 * {@code subscription/plugins/**}, none of which is on any classpath that also carries the engine.
 *
 * @see BooleanAccessorPairingScan for the invariant and why it walks bytecode
 */
class BooleanAccessorPairingTest {

    /** Measured at 4175; the floor sits below that so ordinary growth or pruning does not touch it. */
    private static final int MINIMUM_CLASSES_SCANNED = 3_800;

    /**
     * Every one of these is a type the guard exists to protect, asserted present so that a dependency change which
     * quietly narrows this module's classpath fails here rather than letting the scan report clean because it stopped
     * looking. {@code MaintenanceDetailsClient} is the one this module contributes - it is the last real
     * wrapper-{@code Boolean} accessor in the product, paired only because Lombok happens to generate its companion.
     */
    private static final List<String> CLASSES_PROVING_THE_SCAN_REACHES_WHAT_IT_MUST = List.of(
            "org.bonitasoft.web.rest.model.system.MaintenanceDetailsClient",
            "org.bonitasoft.engine.data.instance.model.SDataInstance",
            "org.bonitasoft.engine.data.definition.model.SDataDefinition",
            "org.bonitasoft.engine.bpm.data.DataInstance",
            "org.bonitasoft.engine.bdm.model.field.Field");

    @Test
    void everyWrapperBooleanIsAccessorInTheCommunityProduct_should_have_a_getX_companion() throws Exception {
        //when-then
        BooleanAccessorPairingScan.assertEveryWrapperBooleanIsAccessorIsPaired(
                MINIMUM_CLASSES_SCANNED, CLASSES_PROVING_THE_SCAN_REACHES_WHAT_IT_MUST);
    }

}
