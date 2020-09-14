/**
 * Copyright (C) 2020 Bonitasoft S.A.
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
package org.bonitasoft.engine.connector.sap;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.sap.conn.jco.ext.DestinationDataProvider;
import com.sap.conn.jco.ext.Environment;
import mockit.Mock;
import mockit.MockUp;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * <b>IMPORTANT</b>: this test class uses jmockit. If you run it in your IDE, you may need to configure a java agent
 * (its
 * seems working out of the box in IntelliJ). See the maven pom file.
 *
 * @author Aurelien Pupier
 * @author Baptiste Mesta
 */
public class SAPMonoDestinationDataProviderTest {

    @Before
    public void setupMocks() {
        // Ugly stuff that does not respect the "don't mock code you don't own"
        new MockUp<Environment>() {

            @Mock
            public void registerDestinationDataProvider(DestinationDataProvider destinationDataProvider) {
                // do nothing
            }

            @Mock
            public void unregisterDestinationDataProvider(DestinationDataProvider destinationDataProvider) {
                // do nothing
            }

        };
    }

    @Before
    public void setup() {
        SAPMonoDestinationDataProvider.clear();
    }

    @After
    public void tearDown() {
        SAPMonoDestinationDataProvider.clear();
    }

    @Test
    public void should_fail_to_set_multitple_different_destinations() throws IllegalStateException {
        SAPMonoDestinationDataProvider.getInstance("name1"); // 1st one, should succeed

        assertThatThrownBy(() -> SAPMonoDestinationDataProvider.getInstance("name2"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageStartingWith("You can use only one SAP destination");
    }

    @Test
    public void should_succeed_to_set_the_same_destination_several_times() throws IllegalStateException {
        SAPMonoDestinationDataProvider.getInstance("nameSimilar");
        SAPMonoDestinationDataProvider.getInstance("nameSimilar");
    }

}
