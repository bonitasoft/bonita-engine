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
import org.springframework.beans.BeansException;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * @author Emmanuel Duchastenier
 */
@Component
public class BonitaEngineEventListener implements ApplicationContextAware {

    private ApplicationContext context;

    private BonitaEngine bonitaEngine;

    @EventListener
    public void handleApplicationReadyEvent(final ApplicationReadyEvent applicationReadyEvent) throws Exception {
        bonitaEngine = applicationReadyEvent.getApplicationContext().getBean(BonitaEngine.class);
        bonitaEngine.start();
    }

    @EventListener
    public void handleContextStoppedEvent(final ContextClosedEvent contextClosedEvent) throws Exception {
        if (bonitaEngine != null && context == contextClosedEvent.getApplicationContext()) {
            bonitaEngine.stop();
        }
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.context = applicationContext;
    }
}
