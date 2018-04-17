/**
 * Copyright (C) 2018 Bonitasoft S.A.
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

package org.bonitasoft.engine.test;

import static org.bonitasoft.engine.test.EJBContainerFactory.setAPIType;
import static org.bonitasoft.engine.test.EJBContainerFactory.setupBonitaSysProps;
import static org.bonitasoft.engine.test.EJBContainerFactory.startTomee;

import org.bonitasoft.engine.BPMRemoteTestsForServers;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * @author Baptiste Mesta
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({ BPMRemoteTestsForServers.class })
public class EJBIT {

    @BeforeClass
    public static void start() throws Exception {
        setupBonitaSysProps();
        startTomee();

        TestEngineImpl.getInstance().start();

        setAPIType();
    }

    @AfterClass
    public static void stop() {
        try {
            TestEngineImpl.getInstance().stop();
        } catch (Exception e) {
            // no need to fail the shutdown for that:
            e.printStackTrace();
        }
    }

}