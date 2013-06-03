/**
 * Copyright (C) 2012-2013 BonitaSoft S.A.
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
package org.bonitasoft.engine.jobs;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.bonitasoft.engine.api.impl.transaction.event.GetMessageEventCouples;
import org.bonitasoft.engine.api.impl.transaction.event.HandleMessageEventCouple;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.commons.transaction.TransactionExecutor;
import org.bonitasoft.engine.core.process.instance.api.event.EventInstanceService;
import org.bonitasoft.engine.core.process.instance.model.builder.BPMInstanceBuilders;
import org.bonitasoft.engine.core.process.instance.model.event.handling.SMessageEventCouple;
import org.bonitasoft.engine.core.process.instance.model.event.handling.SMessageInstance;
import org.bonitasoft.engine.core.process.instance.model.event.handling.SWaitingMessageEvent;
import org.bonitasoft.engine.events.model.FireEventException;
import org.bonitasoft.engine.execution.event.EventsHandler;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.scheduler.JobExecutionException;
import org.bonitasoft.engine.scheduler.SJobConfigurationException;

/**
 * @author Elias Ricken de Medeiros
 * @author Matthieu Chaffotte
 */
public class BPMEventHandlingJob extends InternalJob {

    private static final long serialVersionUID = 5095357357278143665L;

    private transient EventInstanceService eventInstanceService;

    private transient TransactionExecutor transactionExecutor;

    // FIXME improve lock mechanism by tenant map<tenant, lock>
    private static transient Object lock = new Object();

    private transient BPMInstanceBuilders instanceBuilders;

    private EventsHandler enventsHandler;

    private TechnicalLoggerService logger;

    @Override
    public String getName() {
        return "BPMEventHandlingJob";
    }

    @Override
    public String getDescription() {
        return "Handle BPM events";
    }

    @Override
    public void execute() throws JobExecutionException, FireEventException {
        synchronized (lock) {
            try {
                final GetMessageEventCouples getMessageEventCouplesTransaction = new GetMessageEventCouples(eventInstanceService);
                transactionExecutor.execute(getMessageEventCouplesTransaction);
                final List<SMessageEventCouple> messageCouples = getMessageEventCouplesTransaction.getResult();

                final Map<SMessageInstance, List<SWaitingMessageEvent>> messageMapping = new HashMap<SMessageInstance, List<SWaitingMessageEvent>>();
                for (final SMessageEventCouple sMessageEventCouple : messageCouples) {
                    final SWaitingMessageEvent waitingMessage = sMessageEventCouple.getWaitingMessage();
                    final SMessageInstance messageInstance = sMessageEventCouple.getMessageInstance();
                    if (!messageMapping.keySet().contains(messageInstance)) {
                        messageMapping.put(messageInstance, new ArrayList<SWaitingMessageEvent>());
                    }
                    messageMapping.get(messageInstance).add(waitingMessage);
                }
                for (final Entry<SMessageInstance, List<SWaitingMessageEvent>> entry : messageMapping.entrySet()) {
                    final HandleMessageEventCouple handleMessageEventCouple = new HandleMessageEventCouple(entry.getKey(), entry.getValue(),
                            eventInstanceService, instanceBuilders, enventsHandler, logger);
                    transactionExecutor.execute(handleMessageEventCouple);
                }
                // TODO unlock message instances and waiting messages
            } catch (final SBonitaException e) {
                throw new JobExecutionException(e);
            }
        }
    }

    @Override
    public void setAttributes(final Map<String, Serializable> attributes) throws SJobConfigurationException {
        eventInstanceService = getTenantServiceAccessor().getEventInstanceService();
        transactionExecutor = getTenantServiceAccessor().getTransactionExecutor();
        instanceBuilders = getTenantServiceAccessor().getBPMInstanceBuilders();
        enventsHandler = getTenantServiceAccessor().getEventsHandler();
        logger = getTenantServiceAccessor().getTechnicalLoggerService();
    }

}
