/**
 * Copyright (C) 2013 BonitaSoft S.A.
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
package org.bonitasoft.engine.test.synchro;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.Map;

import org.bonitasoft.engine.events.model.SEvent;
import org.bonitasoft.engine.events.model.SHandler;
import org.bonitasoft.engine.events.model.SHandlerExecutionException;
import org.bonitasoft.engine.transaction.BonitaTransactionSynchronization;
import org.bonitasoft.engine.transaction.STransactionNotFoundException;
import org.bonitasoft.engine.transaction.TransactionService;

/**
 * @author Baptiste Mesta
 */
public abstract class AbstractUpdateHandler implements SHandler<SEvent> {

    private final TransactionService transactionService;

    public AbstractUpdateHandler(final TransactionService transactionService) {
        super();
        this.transactionService = transactionService;
    }

    protected abstract Map<String, Serializable> getEvent(final SEvent sEvent);

    @Override
    public void execute(final SEvent sEvent) throws SHandlerExecutionException {
        final Map<String, Serializable> event = getEvent(sEvent);
        Long id = null;
        Object object = null;
        try {
            object = sEvent.getObject();
            final Method method = object.getClass().getMethod("getId");
            final Object invoke = method.invoke(object);
            id = (Long) invoke;
        } catch (final Throwable e) {
            System.out.println("AbstractUpdateHandler: No id on object " + object);
        }
        try {
            final BonitaTransactionSynchronization synchronization = new WaitForEventSynchronization(event, id);
            transactionService.registerBonitaSynchronization(synchronization);
        } catch (final STransactionNotFoundException e) {
            e.printStackTrace();
            throw new SHandlerExecutionException(e);
        }
    }

}
