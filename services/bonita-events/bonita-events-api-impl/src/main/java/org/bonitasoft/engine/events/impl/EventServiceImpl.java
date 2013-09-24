/**
 * Copyright (C) 2011-2013 BonitaSoft S.A.
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

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.bonitasoft.engine.commons.LogUtil;
import org.bonitasoft.engine.events.EventActionType;
import org.bonitasoft.engine.events.EventService;
import org.bonitasoft.engine.events.model.FireEventException;
import org.bonitasoft.engine.events.model.HandlerRegistrationException;
import org.bonitasoft.engine.events.model.HandlerUnregistrationException;
import org.bonitasoft.engine.events.model.SEvent;
import org.bonitasoft.engine.events.model.SHandler;
import org.bonitasoft.engine.events.model.builders.SEventBuilder;
import org.bonitasoft.engine.events.model.builders.SEventBuilders;
import org.bonitasoft.engine.log.technical.TechnicalLogSeverity;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;

/**
 * @author Christophe Havard
 * @author Matthieu Chaffotte
 */
public class EventServiceImpl implements EventService {

    /**
     * Contains a list of all events type and their registered handlers
     */
    protected Map<String, Set<SHandler<SEvent>>> registeredHandlers;

    private final SEventBuilders eventBuilders;

    protected final TechnicalLoggerService logger;

    public EventServiceImpl(final SEventBuilders eventBuilders, final TechnicalLoggerService logger) {
        super();
        this.eventBuilders = eventBuilders;
        this.logger = logger;
        registeredHandlers = new HashMap<String, Set<SHandler<SEvent>>>();
    }

    /**
     * Fire the given Event only to interested handlers
     * 
     * @throws FireEventException
     */
    @Override
    public void fireEvent(final SEvent event) throws FireEventException {
        if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
            logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogBeforeMethod(this.getClass(), "fireEvent"));
        }
        if (event != null) {
            // if at least 1 eventFilter contains a group of handlers for the given event type
            if (containsHandlerFor(event.getType())) {
                // retrieve the handler list concerned by the given event
                final Collection<SHandler<SEvent>> handlers = getHandlersFor(event.getType());

                if (handlers.size() > 0) {
                    FireEventException fireEventException = null;
                    for (final SHandler<SEvent> handler : handlers) {
                        // for each handler, I check if it's interested or not by the given event
                        if (handler.isInterested(event)) {
                            // for now, I just log the Exception into the console
                            try {
                                handler.execute(event);
                            } catch (final Exception e) {
                                if (fireEventException == null) {
                                    fireEventException = new FireEventException("Unable to execute some handler");
                                }
                                fireEventException.addHandlerException(e);
                                if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.ERROR)) {
                                    logger.log(this.getClass(), TechnicalLogSeverity.ERROR, "Unable to execute handler", e);
                                }
                            }
                        }
                    }
                    if (fireEventException != null) {
                        throw fireEventException;
                    }
                }
            }
            if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogAfterMethod(this.getClass(), "fireEvent"));
            }
        } else {
            if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE,
                        LogUtil.getLogOnExceptionMethod(this.getClass(), "fireEvent", "Unable to fire a null event"));
            }
            throw new FireEventException("Unable to fire a null event");
        }
    }

    /**
     * No handler duplication in a list for a given event type
     */
    @Override
    public void addHandler(final String eventType, final SHandler<SEvent> handler) throws HandlerRegistrationException {
        if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
            logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogBeforeMethod(this.getClass(), "addHandler"));
        }
        // the add method returns false if the given element already exists in the Set, and does nothing.
        if (handler != null && eventType != null) {
            addHandlerFor(eventType, handler);
            if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogAfterMethod(this.getClass(), "addHandler"));
            }
        } else {
            if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE,
                        LogUtil.getLogOnExceptionMethod(this.getClass(), "addHandler", "Event type and/or handler is null"));
            }
            throw new HandlerRegistrationException();
        }
    }

    @Override
    public void removeAllHandlers(final SHandler<SEvent> handler) throws HandlerUnregistrationException {
        if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
            logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogBeforeMethod(this.getClass(), "removeAllHandlers"));
        }
        if (handler == null) {
            if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE,
                        LogUtil.getLogOnExceptionMethod(this.getClass(), "removeAllHandlers", "Unable to remove a null event"));
            }
            throw new HandlerUnregistrationException();
        } else {
            removeHandlerInAllType(handler);
        }
        if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
            logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogAfterMethod(this.getClass(), "removeAllHandlers"));
        }
    }

    @Override
    public void removeHandler(final String eventType, final SHandler<SEvent> h) throws HandlerUnregistrationException {
        if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
            logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogBeforeMethod(this.getClass(), "removeHandler"));
        }
        if (h == null || eventType == null) {
            throw new HandlerUnregistrationException();
        }
        // check if the handler has already been registered, then remove it
        final Collection<SHandler<SEvent>> handlers = getHandlersFor(eventType);
        if (handlers != null && handlers.contains(h)) {
            handlers.remove(h);
        } else {
            throw new HandlerUnregistrationException();
        }
        if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
            logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogAfterMethod(this.getClass(), "removeHandler"));
        }
    }

    @Override
    public Set<SHandler<SEvent>> getHandlers(final String eventType) {
        if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
            logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogBeforeMethod(this.getClass(), "getHandlers"));
        }
        final Collection<SHandler<SEvent>> handlers = getHandlersFor(eventType);
        if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
            logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogAfterMethod(this.getClass(), "getHandlers"));
        }
        if (handlers == null) {
            return Collections.emptySet();
        }
        HashSet<SHandler<SEvent>> hashSet = new HashSet<SHandler<SEvent>>(handlers.size());
        hashSet.addAll(handlers);
        return hashSet;

    }

    @Override
    public SEventBuilder getEventBuilder() {
        if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
            logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogBeforeMethod(this.getClass(), "getEventBuilder"));
        }
        final SEventBuilder sEventBuilder = eventBuilders.getEventBuilder();
        if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
            logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogAfterMethod(this.getClass(), "getEventBuilder"));
        }
        return sEventBuilder;
    }

    @Override
    public boolean hasHandlers(final String eventType, final EventActionType actionType) {
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

    protected boolean containsHandlerFor(final String key) {
        return registeredHandlers.containsKey(key);
    }

    protected Collection<SHandler<SEvent>> getHandlersFor(final String eventType) {
        return registeredHandlers.get(eventType);
    }

    protected void addHandlerFor(final String eventType, final SHandler<SEvent> handler) throws HandlerRegistrationException {
        logger.log(this.getClass(), TechnicalLogSeverity.TRACE, "NON-cluster implementation of addHandlerFor() method for eventType=" + eventType
                + ", handler=" + handler.getClass().getSimpleName());
        // check if the given event type is already registered in the Event Service
        if (containsHandlerFor(eventType)) {
            // if the handler already exists for the same eventType, an Exception is thrown
            final Set<SHandler<SEvent>> handlers = registeredHandlers.get(eventType);
            if (!handlers.add(handler)) {
                throw new HandlerRegistrationException("This handler is already registered for this event type");
            }
        } else {
            // if the given type doesn't already exist in the eventFilters list, we create it
            final Set<SHandler<SEvent>> newHandlerSet = new HashSet<SHandler<SEvent>>();
            newHandlerSet.add(handler);
            registeredHandlers.put(eventType, newHandlerSet);
        }
    }

    protected void removeHandlerInAllType(final SHandler<SEvent> handler) {
        for (final Set<SHandler<SEvent>> handlers : registeredHandlers.values()) {
            if (handlers.contains(handler)) {
                handlers.remove(handler);
            }
        }
    }
}
