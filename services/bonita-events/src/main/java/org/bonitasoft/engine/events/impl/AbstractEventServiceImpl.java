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
package org.bonitasoft.engine.events.impl;

import org.bonitasoft.engine.events.EventActionType;
import org.bonitasoft.engine.events.EventService;
import org.bonitasoft.engine.events.model.HandlerRegistrationException;
import org.bonitasoft.engine.events.model.HandlerUnregistrationException;
import org.bonitasoft.engine.events.model.SEvent;
import org.bonitasoft.engine.events.model.SFireEventException;
import org.bonitasoft.engine.events.model.SHandler;
import org.bonitasoft.engine.log.technical.TechnicalLogSeverity;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public abstract class AbstractEventServiceImpl implements EventService {

    protected static TechnicalLoggerService logger;

    protected AbstractEventServiceImpl(final TechnicalLoggerService logger) {
        AbstractEventServiceImpl.logger = logger;
    }

    /**
     * Fire the given Event only to interested handlers
     *
     * @throws SFireEventException
     */
    @Override
    public void fireEvent(final SEvent event) throws SFireEventException {
        if (event == null) {
            throw new SFireEventException("Event is null");
        }
        // if at least 1 eventFilter contains a group of handlers for the given event type
        if (containsHandlerFor(event.getType())) {
            // retrieve the handler list concerned by the given event
            final Collection<SHandler<SEvent>> handlers = getHandlersFor(event.getType());

            if (handlers.size() > 0) {
                if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
                    logger.log(this.getClass(), TechnicalLogSeverity.TRACE, "Found " + handlers.size() + " for event " + event.getType()
                            + ". All handlers: " + handlers);
                }
                SFireEventException sFireEventException = null;
                for (final SHandler<SEvent> handler : handlers) {
                    // for each handler, I check if it's interested or not by the given event
                    try {
                        if (handler.isInterested(event)) {
                            handler.execute(event);
                        }
                    } catch (final Exception e) {
                        if (sFireEventException == null) {
                            sFireEventException = new SFireEventException("Unable to execute some handler.", e);
                        }
                        sFireEventException.addHandlerException(e);
                        if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.DEBUG)) {
                            logger.log(this.getClass(), TechnicalLogSeverity.DEBUG, "Handler failed", e);
                        }
                    }
                }
                if (sFireEventException != null) {
                    throw sFireEventException;
                }
            }
        }
    }

    protected abstract Collection<SHandler<SEvent>> getHandlersFor(final String type);

    protected abstract boolean containsHandlerFor(final String type);

    /**
     * No handler duplication in a list for a given event type
     */
    @Override
    public final void addHandler(final String eventType, final SHandler<SEvent> handler) throws HandlerRegistrationException {
        if (handler != null && eventType != null) {
            addHandlerFor(eventType, handler);
        } else {
            throw new HandlerRegistrationException("One of the parameters is null : " + " eventType: " + eventType + " handler:" + handler);
        }
    }

    protected abstract void addHandlerFor(String eventType, SHandler<SEvent> handler) throws HandlerRegistrationException;

    @Override
    public final void removeAllHandlers(final SHandler<SEvent> handler) throws HandlerUnregistrationException {
        if (handler == null) {
            throw new HandlerUnregistrationException("Unable to remove a null handler");
        }
        removeAllHandlersFor(handler);
    }

    protected abstract void removeAllHandlersFor(SHandler<SEvent> handler);

    @Override
    public final void removeHandler(final String eventType, final SHandler<SEvent> h) throws HandlerUnregistrationException {
        if (h == null || eventType == null) {
            throw new HandlerUnregistrationException("Unable to remove a null handler");
        }
        removeHandlerFor(eventType, h);
    }

    /**
     * @param eventType
     * @param h
     * @throws HandlerUnregistrationException
     */
    protected abstract void removeHandlerFor(final String eventType, final SHandler<SEvent> h) throws HandlerUnregistrationException;

    @Override
    public final Set<SHandler<SEvent>> getHandlers(final String eventType) {
        final Collection<SHandler<SEvent>> handlers = getHandlersFor(eventType);
        if (handlers == null) {
            return Collections.emptySet();
        }
        return new HashSet<>(handlers);
    }

    @Override
    public final boolean hasHandlers(final String eventType, final EventActionType actionType) {
        String key = eventType;
        if (actionType != null) {
            switch (actionType) {
                case CREATED:
                    key += SEvent.CREATED;
                    break;
                case DELETED:
                    key += SEvent.DELETED;
                    break;
                case UPDATED:
                    key += SEvent.UPDATED;
                    break;
                default:
                    return false;
            }
        }
        return containsHandlerFor(key);
    }

}
