/**
 * Copyright (C) 2025 Bonitasoft S.A.
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
package org.bonitasoft.engine.business.data.impl;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.bonitasoft.engine.bdm.Entity;
import org.bonitasoft.engine.events.EventService;
import org.bonitasoft.engine.events.model.SDeleteEvent;
import org.bonitasoft.engine.events.model.SEvent;
import org.bonitasoft.engine.events.model.SFireEventException;
import org.bonitasoft.engine.events.model.SInsertEvent;
import org.bonitasoft.engine.events.model.SUpdateEvent;
import org.springframework.stereotype.Component;

/**
 * Aspect to handle events for business data repository operations.
 * Fires events on persist, merge, and remove operations.
 * This aspect is enabled in EngineConfiguration class through annotation @EnableAspectJAutoProxy.
 */
@Aspect
@Component
public class BusinessDataRepositoryEventAspect {

    private final EventService eventService;

    public BusinessDataRepositoryEventAspect(EventService eventService) {
        this.eventService = eventService;
    }

    @Around("execution(* org.bonitasoft.engine.business.data.BusinessDataRepository.persist(..)) && args(entity)")
    public Object aroundPersist(ProceedingJoinPoint target, Entity entity) throws Throwable {
        return persistOrMerge(target, entity);
    }

    @Around("execution(* org.bonitasoft.engine.business.data.BusinessDataRepository.merge(..)) && args(entity)")
    public Object aroundMerge(ProceedingJoinPoint target, Entity entity) throws Throwable {
        return persistOrMerge(target, entity);
    }

    private Object persistOrMerge(ProceedingJoinPoint target, Entity entity) throws Throwable {
        if (entity != null) {
            var originalPersistenceId = entity.getPersistenceId();
            Object result = target.proceed();
            var event = (originalPersistenceId == null) ? new SInsertEvent(getEventType(SEvent.CREATED))
                    : new SUpdateEvent(getEventType(SEvent.UPDATED));
            event.setObject(entity);
            eventService.fireEvent(event);
            return result;
        }
        return null;
    }

    @AfterReturning("execution(* org.bonitasoft.engine.business.data.BusinessDataRepository.remove(..)) && args(entity)")
    public void afterRemove(Entity entity) throws SFireEventException {
        fireDeleteEvent(entity);
    }

    @AfterReturning(pointcut = "execution(* org.bonitasoft.engine.business.data.BusinessDataRepository.removeById(..))", returning = "entity")
    public void afterRemoveById(Entity entity) throws SFireEventException {
        fireDeleteEvent(entity);
    }

    private void fireDeleteEvent(Entity entity) throws SFireEventException {
        var event = new SDeleteEvent(getEventType(SEvent.DELETED));
        event.setObject(entity);
        eventService.fireEvent(event);
    }

    private static String getEventType(String type) {
        return "BUSINESS_DATA" + type;
    }
}
