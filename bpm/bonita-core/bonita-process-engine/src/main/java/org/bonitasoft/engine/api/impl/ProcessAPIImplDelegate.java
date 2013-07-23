package org.bonitasoft.engine.api.impl;

import java.io.File;
import java.io.IOException;

import org.bonitasoft.engine.api.impl.transaction.process.DeleteProcess;
import org.bonitasoft.engine.api.impl.transaction.process.DisableProcess;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.commons.transaction.TransactionContent;
import org.bonitasoft.engine.core.process.definition.ProcessDefinitionService;
import org.bonitasoft.engine.core.process.definition.SProcessDefinitionNotFoundException;
import org.bonitasoft.engine.core.process.instance.api.event.EventInstanceService;
import org.bonitasoft.engine.exception.BonitaHomeNotSetException;
import org.bonitasoft.engine.home.BonitaHomeServer;
import org.bonitasoft.engine.io.IOUtil;
import org.bonitasoft.engine.scheduler.SchedulerService;
import org.bonitasoft.engine.service.PlatformServiceAccessor;
import org.bonitasoft.engine.service.TenantServiceAccessor;
import org.bonitasoft.engine.service.TenantServiceSingleton;
import org.bonitasoft.engine.service.impl.ServiceAccessorFactory;
import org.bonitasoft.engine.sessionaccessor.SessionAccessor;

public class ProcessAPIImplDelegate {

	protected static TenantServiceAccessor getTenantAccessor() {
		try {
			final SessionAccessor sessionAccessor = ServiceAccessorFactory.getInstance().createSessionAccessor();
			final long tenantId = sessionAccessor.getTenantId();
			return TenantServiceSingleton.getInstance(tenantId);
		} catch (final Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	private static PlatformServiceAccessor getPlatformServiceAccessor() {
		try {
			return ServiceAccessorFactory.getInstance().createPlatformServiceAccessor();
		} catch (final Exception e) {
			throw new RuntimeException(e);
		}
	}

	public void deleteProcessDefinition(final long processId) throws SBonitaException, BonitaHomeNotSetException, IOException {
		final TenantServiceAccessor tenantAccessor = getTenantAccessor();

		DeleteProcess deleteProcess = instantiateDeleteProcessTransactionContent(processId);
		deleteProcess.execute();

		final String processesFolder = BonitaHomeServer.getInstance().getProcessesFolder(tenantAccessor.getTenantId());
		final File file = new File(processesFolder);
		if (!file.exists()) {
			file.mkdir();
		}

		final File processFolder = new File(file, String.valueOf(processId));
		IOUtil.deleteDir(processFolder);

	}

    protected DeleteProcess instantiateDeleteProcessTransactionContent(final long processId) {
        return new DeleteProcess(getTenantAccessor(), processId);
    }
	
	public void disableProcess(final long processId) throws SProcessDefinitionNotFoundException, SBonitaException {
		final TenantServiceAccessor tenantAccessor = getTenantAccessor();
		final PlatformServiceAccessor platformServiceAccessor = getPlatformServiceAccessor();
		final ProcessDefinitionService processDefinitionService = tenantAccessor.getProcessDefinitionService();

		final EventInstanceService eventInstanceService = tenantAccessor.getEventInstanceService();
		final SchedulerService schedulerService = platformServiceAccessor.getSchedulerService();
		final TransactionContent transactionContent = new DisableProcess(processDefinitionService, processId, eventInstanceService, schedulerService);
			transactionContent.execute();
	}

}
