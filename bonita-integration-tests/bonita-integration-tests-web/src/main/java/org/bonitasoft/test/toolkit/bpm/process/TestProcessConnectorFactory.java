/**
 * Copyright (C) 2022 Bonitasoft S.A.
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
package org.bonitasoft.test.toolkit.bpm.process;

import org.bonitasoft.engine.bpm.connector.ConnectorEvent;

/**
 * @author Colin PUY
 */
public final class TestProcessConnectorFactory {

    public static TestProcessConnector getDefaultConnector() {
        TestProcessConnector testProcessConnector = new TestProcessConnector("aConnector",
                "org.bonitasoft.test.toolkit.connector.TestConnector", "1.0",
                "org.bonitasoft.test.toolkit.connector.TestConnector",
                "org.bonitasoft.test.toolkit.connector.testConnector", ConnectorEvent.ON_ENTER,
                "TestConnector.impl", "/org/bonitasoft/test/toolkit/connector/TestConnector.impl");
        testProcessConnector.addDependency("aDependency.jar").addDependency("anOtherDependency.jar");
        return testProcessConnector;
    }

}
