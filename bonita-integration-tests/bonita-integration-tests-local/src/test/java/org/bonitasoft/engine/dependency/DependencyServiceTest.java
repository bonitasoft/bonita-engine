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
package org.bonitasoft.engine.dependency;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;

import java.util.List;

import org.bonitasoft.engine.bpm.CommonBPMServicesTest;
import org.bonitasoft.engine.dependency.model.SDependency;
import org.bonitasoft.engine.dependency.model.ScopeType;
import org.junit.Test;

/**
 * @author Charles Souillard
 */
public class DependencyServiceTest extends CommonBPMServicesTest {

    private static DependencyService dependencyService;

    public DependencyServiceTest() {
        dependencyService = getTenantAccessor().getDependencyService();
    }

    private final String defaultName = "abc";

    private final String defaultFileName = "dfv.cu";

    private final byte[] defaultValue = new byte[]{12, 33};

    @Test
    public void testLifeCycle() throws Exception {
        getTransactionService().begin();

        SDependency mappedDependency = dependencyService.createMappedDependency(defaultName, defaultValue, defaultFileName, 2L, ScopeType.PROCESS);

        List<Long> dependencyIds = dependencyService.getDependencyIds(2L, ScopeType.PROCESS, 0, 1);

        assertThat(mappedDependency.getFileName()).isEqualTo(defaultFileName);
        assertThat(mappedDependency.getName()).isEqualTo("2_" + defaultName);
        assertThat(mappedDependency.getValue()).isEqualTo(defaultValue);
        assertThat(dependencyIds.get(0)).isEqualTo(mappedDependency.getId());

        dependencyService.deleteDependencies(2L, ScopeType.PROCESS);

        try {
            dependencyService.getDependency(mappedDependency.getId());
            fail("dependency with id: " + mappedDependency.getId() + " must not be found!");
        } catch (final SDependencyNotFoundException e) {
            // OK
        }

        getTransactionService().complete();
    }

}
