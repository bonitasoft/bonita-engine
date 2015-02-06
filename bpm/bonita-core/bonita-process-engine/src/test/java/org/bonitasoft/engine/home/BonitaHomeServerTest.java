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
package org.bonitasoft.engine.home;

import static org.junit.Assert.assertEquals;

import java.io.File;

import org.bonitasoft.engine.exception.BonitaException;
import org.bonitasoft.engine.exception.BonitaHomeNotSetException;
import org.junit.Test;

/**
 * @author Baptiste Mesta
 */
public class BonitaHomeServerTest {

    @Test(expected = BonitaHomeNotSetException.class)
    public void testBonitaHomeServerNotSet() throws BonitaException {
        System.setProperty(BonitaHome.BONITA_HOME, "");// same as not set
        final BonitaHomeServer bonitaHome = BonitaHomeServer.getInstance();
        bonitaHome.refreshBonitaHome();
        bonitaHome.getBonitaHomeServerFolder();
    }

    @Test
    public void testGetBonitaHomeServerPath() throws BonitaException {
        final String systemPropertyPath = System.getProperty("user.home");
        System.setProperty(BonitaHome.BONITA_HOME, systemPropertyPath);
        final BonitaHomeServer bonitaHome = BonitaHomeServer.getInstance();
        final String bonitaHomeServerFolder = bonitaHome.getBonitaHomeServerFolder();
        assertEquals(System.getProperty("user.home") + File.separatorChar + "server", bonitaHomeServerFolder);
    }

    @Test
    public void testGetBonitaHomePlatform() throws BonitaException {
        final String systemPropertyPath = System.getProperty("user.home");
        System.setProperty(BonitaHome.BONITA_HOME, systemPropertyPath);
        final BonitaHomeServer bonitaHome = BonitaHomeServer.getInstance();
        final String bonitaHomeServerFolder = bonitaHome.getPlatformFolder();
        assertEquals(System.getProperty("user.home") + File.separatorChar + "server" + File.separatorChar + "platform", bonitaHomeServerFolder);
    }

    @Test
    public void testGetBonitaHomePlatformConf() throws BonitaException {
        final String systemPropertyPath = System.getProperty("user.home");
        System.setProperty(BonitaHome.BONITA_HOME, systemPropertyPath);
        final BonitaHomeServer bonitaHome = BonitaHomeServer.getInstance();
        final String bonitaHomeServerFolder = bonitaHome.getPlatformConfFolder();
        assertEquals(System.getProperty("user.home") + File.separatorChar + "server" + File.separatorChar + "platform" + File.separatorChar + "conf",
                bonitaHomeServerFolder);
    }

    @Test
    public void testGetBonitaHomeTenants() throws BonitaException {
        final String systemPropertyPath = System.getProperty("user.home");
        System.setProperty(BonitaHome.BONITA_HOME, systemPropertyPath);
        final BonitaHomeServer bonitaHome = BonitaHomeServer.getInstance();
        final String bonitaHomeServerFolder = bonitaHome.getTenantsFolder();
        assertEquals(System.getProperty("user.home") + File.separatorChar + "server" + File.separatorChar + "tenants", bonitaHomeServerFolder);
    }

    @Test
    public void testGetBonitaHomeTenantByName() throws BonitaException {
        final String systemPropertyPath = System.getProperty("user.home");
        System.setProperty(BonitaHome.BONITA_HOME, systemPropertyPath);
        final BonitaHomeServer bonitaHome = BonitaHomeServer.getInstance();
        final String bonitaHomeServerFolder = bonitaHome.getTenantFolder(1);
        assertEquals(System.getProperty("user.home") + File.separatorChar + "server" + File.separatorChar + "tenants" + File.separatorChar + "1",
                bonitaHomeServerFolder);
    }

    @Test
    public void testGetBonitaHomeTenantConf() throws BonitaException {
        final String systemPropertyPath = System.getProperty("user.home");
        System.setProperty(BonitaHome.BONITA_HOME, systemPropertyPath);
        final BonitaHomeServer bonitaHome = BonitaHomeServer.getInstance();
        final String bonitaHomeServerFolder = bonitaHome.getTenantConfFolder(1);
        assertEquals(System.getProperty("user.home") + File.separatorChar + "server" + File.separatorChar + "tenants" + File.separatorChar + "1"
                + File.separatorChar + "conf", bonitaHomeServerFolder);
    }
}
