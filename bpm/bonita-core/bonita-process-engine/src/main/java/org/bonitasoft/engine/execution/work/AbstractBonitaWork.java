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
package org.bonitasoft.engine.execution.work;

import java.util.concurrent.Callable;

import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.log.technical.TechnicalLogSeverity;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.service.TenantServiceAccessor;
import org.bonitasoft.engine.service.TenantServiceSingleton;
import org.bonitasoft.engine.session.SSessionNotFoundException;
import org.bonitasoft.engine.session.SessionService;
import org.bonitasoft.engine.session.model.SSession;
import org.bonitasoft.engine.sessionaccessor.SessionAccessor;
import org.bonitasoft.engine.transaction.TransactionService;
import org.bonitasoft.engine.work.BonitaWork;

/**
 * @author Emmanuel Duchastenier
 * @author Celine Souchet
 */
public abstract class AbstractBonitaWork implements BonitaWork {

	private static final long serialVersionUID = -3346630968791356467L;

	protected transient TechnicalLoggerService loggerService;

	private transient SessionService sessionService;

	private transient SessionAccessor sessionAccessor;

	private long tenantId;

	protected transient TransactionService transactionService;

	public AbstractBonitaWork() {
		super();
	}

	@Override
	public final void run() {
		TenantServiceAccessor tenantAccessor = getTenantAccessor();
		loggerService = tenantAccessor.getTechnicalLoggerService();
		sessionAccessor = tenantAccessor.getSessionAccessor();
		sessionService = tenantAccessor.getSessionService();
		transactionService = tenantAccessor.getTransactionService();
		SSession session = null;
		boolean canBeExecuted = false; 
		try {
			session = sessionService.createSession(tenantId, "workservice");
			sessionAccessor.setSessionInfo(session.getId(), session.getTenantId());

			loggerService.log(getClass(), TechnicalLogSeverity.DEBUG, "Starting work: " + getDescription());
			canBeExecuted = preWork();
			if (canBeExecuted) {
				if (isTransactional()) {
					workInTransaction();
				} else {
					work();
				}
			}
		} catch (final SBonitaException e) {
			handleError(e);
		} catch (final Exception e) {
			// Edge case we cannot manage
			loggerService.log(getClass(), TechnicalLogSeverity.ERROR,
					"Unexpected error while executing work. You may consider restarting the system. This will restart all works.", e);
		} finally {
			if (canBeExecuted) {
				try {
	                afterWork();
                } catch (Exception e) {
                	loggerService.log(this.getClass(), TechnicalLogSeverity.ERROR, e);
                }
			}
			if (session != null) {
				try {
					sessionAccessor.deleteSessionId();
					sessionService.deleteSession(session.getId());
				} catch (final SSessionNotFoundException e) {
					loggerService.log(this.getClass(), TechnicalLogSeverity.DEBUG, e);
				}
			}
		}
	}

	protected abstract boolean isTransactional();

	protected boolean preWork() throws Exception {
		//DO NOTHING BY DEFAULT
		return true;
	}

	protected void afterWork() throws Exception {
		//DO NOTHING BY DEFAULT   
	}

	protected abstract void work() throws Exception;

	protected void workInTransaction() throws Exception {
		final Callable<Void> runWork = new Callable<Void>() {

			@Override
			public Void call() throws Exception {
				work();
				return null;
			}
		};

		// Call the method work() wrapped in a transaction.
		transactionService.executeInTransaction(runWork);
	}

	protected void handleError(final SBonitaException e) {
		throw new IllegalStateException("Must be implemented in sub-classes to handle Set Failed, or log severe message with procedure to restart.", e);
	}

	protected long getTenantId() {
		return tenantId;
	}

	@Override
	public void setTenantId(final long tenantId) {
		this.tenantId = tenantId;
	}

	protected TenantServiceAccessor getTenantAccessor() {
		try {
			return TenantServiceSingleton.getInstance(getTenantId());
		} catch (final Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public String toString() {
		return "Work[" + getDescription() + "]";
	}
}
