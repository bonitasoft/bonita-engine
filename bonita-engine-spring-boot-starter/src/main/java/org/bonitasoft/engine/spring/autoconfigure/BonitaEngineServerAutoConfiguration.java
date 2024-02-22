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

import org.bonitasoft.engine.BonitaEngine;
import org.bonitasoft.engine.api.APIClient;
import org.bonitasoft.engine.spring.autoconfigure.properties.BonitaEngineProperties;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.AutoConfigureOrder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;

@Configuration
@AutoConfigureBefore(BonitaEngineCommonAutoConfiguration.class)
@AutoConfigureOrder(Ordered.LOWEST_PRECEDENCE)
public class BonitaEngineServerAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    BonitaEngine bonitaEngine(BonitaEngineProperties properties) {
        BonitaEngine instance = new BonitaEngine();
        instance.setBonitaDatabaseConfiguration(properties.getDatabase().getBonita());
        instance.setBusinessDataDatabaseConfiguration(properties.getDatabase().getBusinessData());
        return instance;
    }

    @Bean
    @ConditionalOnMissingBean
    APIClient bonitaClient() {
        return new APIClient();
    }

}
