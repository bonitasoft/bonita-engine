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
package org.bonitasoft.engine.synchro;

import java.io.Serializable;
import java.util.Map;

import javax.jms.JMSException;

import org.bonitasoft.engine.transaction.BonitaTransactionSynchronization;
import org.bonitasoft.engine.transaction.TransactionState;

/**
 * @author Baptiste Mesta
 */
public class SendJMSMessageSynchronization implements BonitaTransactionSynchronization {

    private final Map<String, Serializable> event;

    private final Long id;

    private final JMSProducer jmsProducer;

    public SendJMSMessageSynchronization(final Map<String, Serializable> event, final Long id, final JMSProducer jmsProducer) {
        this.event = event;
        this.id = id;
        this.jmsProducer = jmsProducer;
    }

    @Override
    public void beforeCommit() {
        // NOTHING
    }

    @Override
    public void afterCompletion(final TransactionState status) {
        if (status == TransactionState.COMMITTED) {
        	try {
	            jmsProducer.sendMessage(event, Long.toString(id));
            } catch (JMSException e) {
	            e.printStackTrace();
	            throw new RuntimeException(e);
            }
        }
    }

}
