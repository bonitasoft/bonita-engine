/**
 * Copyright (C) 2019 Bonitasoft S.A.
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
package org.bonitasoft.engine.bpm;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.bonitasoft.engine.commons.PlatformRestartHandler;
import org.bonitasoft.engine.exception.BonitaException;
import org.bonitasoft.engine.platform.configuration.NodeConfiguration;
import org.bonitasoft.engine.platform.configuration.NodeConfigurationImpl;
import org.bonitasoft.engine.service.PlatformServiceAccessor;
import org.bonitasoft.engine.service.impl.ServiceAccessorFactory;
import org.junit.BeforeClass;
import org.junit.Test;

public class NodeConfigurationIT {

    public static NodeConfiguration nodeConfiguration;

    @BeforeClass
    public static void beforeClass() throws BonitaException {
        PlatformServiceAccessor platformAccessor;
        try {
            platformAccessor = ServiceAccessorFactory.getInstance().createPlatformServiceAccessor();
        } catch (Exception ex) {
            throw new BonitaException(ex);
        }
        nodeConfiguration = platformAccessor.getPlatformConfiguration();
    }

    @Test
    public void setRestartHandlers() {
        List<PlatformRestartHandler> lPlatformRestartHandlers = new ArrayList<>();
        PlatformRestartHandler rh = () -> {

        };
        lPlatformRestartHandlers.add(rh);
        ((NodeConfigurationImpl) nodeConfiguration).setPlatformRestartHandlers(lPlatformRestartHandlers);
        assertEquals(1, nodeConfiguration.getPlatformRestartHandlers().size());
    }

    @Test
    public void getShouldRestartElements() {
        assertTrue(nodeConfiguration.shouldResumeElements());
    }

}
