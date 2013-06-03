package org.bonitasoft.engine.test.synchro;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bonitasoft.engine.command.CommandService;
import org.bonitasoft.engine.command.SCommandExecutionException;
import org.bonitasoft.engine.command.SCommandParameterizationException;
import org.bonitasoft.engine.command.TenantCommand;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.commons.transaction.TransactionExecutor;
import org.bonitasoft.engine.events.EventService;
import org.bonitasoft.engine.events.model.SEvent;
import org.bonitasoft.engine.events.model.SHandler;
import org.bonitasoft.engine.recorder.model.EntityUpdateDescriptor;
import org.bonitasoft.engine.service.TenantServiceAccessor;
import org.bonitasoft.engine.transaction.STransactionException;
import org.bonitasoft.engine.transaction.TransactionService;

public class AddHandlerCommand extends TenantCommand {

    private static final String PROCESSINSTANCE_STATE_UPDATED = "PROCESSINSTANCE_STATE_UPDATED";

    private static final String ACTIVITYINSTANCE_STATE_UPDATED = "ACTIVITYINSTANCE_STATE_UPDATED";

    private static final String ACTIVITYINSTANCE_CREATED = "ACTIVITYINSTANCE_CREATED";

    private static final String EVENT_INSTANCE_CREATED = "EVENT_INSTANCE_CREATED";

    @Override
    public Serializable execute(final Map<String, Serializable> parameters, final TenantServiceAccessor serviceAccessor)
            throws SCommandParameterizationException, SCommandExecutionException {
        final EventService eventService = serviceAccessor.getEventService();
        final TransactionService transactionService = serviceAccessor.getTransactionService();
        final TransactionExecutor transactionExecutor = serviceAccessor.getTransactionExecutor();
        try {
            boolean txOpened = transactionExecutor.openTransaction();
            try {
                if (!containsHandler(eventService, PROCESSINSTANCE_STATE_UPDATED, ProcessInstanceHandler.class)) {
                    eventService.addHandler(PROCESSINSTANCE_STATE_UPDATED, new ProcessInstanceHandler(transactionService));
                }
                if (!containsHandler(eventService, ACTIVITYINSTANCE_STATE_UPDATED, FlowNodeHandler.class)) {
                    eventService.addHandler(ACTIVITYINSTANCE_STATE_UPDATED, new FlowNodeHandler(transactionService));
                    eventService.addHandler(ACTIVITYINSTANCE_CREATED, new FlowNodeHandler(transactionService));
                    eventService.addHandler(EVENT_INSTANCE_CREATED, new FlowNodeHandler(transactionService));
                }
                final CommandService commandService = serviceAccessor.getCommandService();
                final List<Long> commandIds = (List<Long>) parameters.get("commands");
                final EntityUpdateDescriptor entityUpdateDescriptor = new EntityUpdateDescriptor();
                entityUpdateDescriptor.addField("system", true);
                for (final Long commandId : commandIds) {
                    commandService.update(commandService.get(commandId), entityUpdateDescriptor);
                }
            } catch (final SBonitaException e) {
                throw new SCommandExecutionException(e);
            } finally {
                transactionExecutor.completeTransaction(txOpened);
            }
        } catch (final STransactionException e) {
            throw new SCommandExecutionException(e);
        }
        return null;
    }

    private boolean containsHandler(final EventService eventService, final String eventType, final Class<?> clazz) {
        final Set<SHandler<SEvent>> handlers = eventService.getHandlers(eventType);
        if (handlers != null) {
            for (final SHandler<SEvent> sHandler : handlers) {
                if (clazz.isInstance(sHandler)) {
                    return true;
                }
            }
        }
        return false;
    }

}
