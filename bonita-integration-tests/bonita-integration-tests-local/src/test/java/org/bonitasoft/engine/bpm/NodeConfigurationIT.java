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

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.bonitasoft.engine.commons.PlatformRestartHandler;
import org.bonitasoft.engine.exception.BonitaException;
import org.bonitasoft.engine.handler.SchedulerServiceRestartHandler;
import org.bonitasoft.engine.platform.configuration.NodeConfiguration;
import org.bonitasoft.engine.service.ServiceAccessor;
import org.bonitasoft.engine.service.impl.ServiceAccessorFactory;
import org.junit.BeforeClass;
import org.junit.Test;

public class NodeConfigurationIT {

    public static NodeConfiguration nodeConfiguration;

    @BeforeClass
    public static void beforeClass() throws BonitaException {
        ServiceAccessor serviceAccessor;
        try {
            serviceAccessor = ServiceAccessorFactory.getInstance().createServiceAccessor();
        } catch (Exception ex) {
            throw new BonitaException(ex);
        }
        nodeConfiguration = serviceAccessor.getPlatformConfiguration();
    }

    @Test
    public void should_have_at_least_one_restart_handler() {
        List<PlatformRestartHandler> platformRestartHandlers = nodeConfiguration.getPlatformRestartHandlers();
        assertThat(platformRestartHandlers).hasSize(1);
        assertThat(platformRestartHandlers.get(0)).isInstanceOf(SchedulerServiceRestartHandler.class);
    }

    @Test
    public void should_clear_sessions() {
        assertThat(nodeConfiguration.shouldClearSessions()).isTrue();
    }
}
