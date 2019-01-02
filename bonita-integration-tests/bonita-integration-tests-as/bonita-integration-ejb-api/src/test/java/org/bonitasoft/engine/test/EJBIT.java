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

import java.net.URL;
import java.util.Enumeration;

import org.bonitasoft.engine.BPMRemoteTestsForServers;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Baptiste Mesta
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({ BPMRemoteTestsForServers.class })
public class EJBIT {

    private static final Logger LOG = LoggerFactory.getLogger(EJBIT.class);

    @BeforeClass
    public static void start() throws Exception {
        LOG.info("Listing ejb-jar.xml files available in the classpath");
        Enumeration<URL> ejbJars = EJBIT.class.getClassLoader().getResources("META-INF/ejb-jar.xml");
        while (ejbJars.hasMoreElements()) {
            URL url = ejbJars.nextElement();
            LOG.info("app = {}", url);
        }
        LOG.info("Listing done");


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
            LOG.warn("Unable to stop the engine, ignoring the error", e);
        }
    }

}