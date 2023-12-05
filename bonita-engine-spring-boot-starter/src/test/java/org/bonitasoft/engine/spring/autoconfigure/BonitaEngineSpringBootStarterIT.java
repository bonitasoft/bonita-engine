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
package org.bonitasoft.engine.spring.autoconfigure;

import org.bonitasoft.engine.api.APIClient;
import org.bonitasoft.engine.platform.LoginException;
import org.junit.Test;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;

public class BonitaEngineSpringBootStarterIT {

    @Test
    public void should_start_engine_when_application_starts() throws LoginException {
        final ConfigurableApplicationContext context = SpringApplication.run(ClientTestApplication.class);
        context.getBean(APIClient.class).login("install", "install");
        context.stop();
    }

}
