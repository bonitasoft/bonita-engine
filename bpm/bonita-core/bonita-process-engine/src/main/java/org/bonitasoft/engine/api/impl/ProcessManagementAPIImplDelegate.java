package org.bonitasoft.engine.api.impl;

import java.io.File;
import java.io.IOException;

import org.bonitasoft.engine.api.impl.transaction.process.DeleteProcess;
import org.bonitasoft.engine.api.impl.transaction.process.DisableProcess;
import org.bonitasoft.engine.classloader.ClassLoaderService;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.commons.transaction.TransactionContent;
import org.bonitasoft.engine.core.process.definition.ProcessDefinitionService;
import org.bonitasoft.engine.core.process.definition.exception.SProcessDefinitionNotFoundException;
import org.bonitasoft.engine.core.process.instance.api.event.EventInstanceService;
import org.bonitasoft.engine.exception.BonitaHomeNotSetException;
import org.bonitasoft.engine.home.BonitaHomeServer;
import org.bonitasoft.engine.io.IOUtil;
import org.bonitasoft.engine.log.technical.TechnicalLogSeverity;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.scheduler.SchedulerService;
import org.bonitasoft.engine.service.PlatformServiceAccessor;
import org.bonitasoft.engine.service.TenantServiceAccessor;
import org.bonitasoft.engine.service.TenantServiceSingleton;
import org.bonitasoft.engine.service.impl.ServiceAccessorFactory;
import org.bonitasoft.engine.sessionaccessor.SessionAccessor;

// Uncomment the "implements" when this delegate implements all the methods.
public class ProcessManagementAPIImplDelegate /* implements ProcessManagementAPI */{

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

    public void deleteProcessDefinition(final long processDefinitionId) throws SBonitaException, BonitaHomeNotSetException, IOException {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final TechnicalLoggerService logger = tenantAccessor.getTechnicalLoggerService();

        final DeleteProcess deleteProcess = instantiateDeleteProcessTransactionContent(processDefinitionId);
        deleteProcess.execute();

        final String processesFolder = BonitaHomeServer.getInstance().getProcessesFolder(tenantAccessor.getTenantId());
        final File file = new File(processesFolder);
        if (!file.exists()) {
            file.mkdir();
        }

        final File processFolder = new File(file, String.valueOf(processDefinitionId));
        IOUtil.deleteDir(processFolder);
        if (logger.isLoggable(getClass(), TechnicalLogSeverity.INFO)) {
            logger.log(this.getClass(), TechnicalLogSeverity.INFO, "The user <" + SessionInfos.getUserNameFromSession() + "> has deleted process with id = <"
                    + processDefinitionId + ">");
        }
    }

    @Deprecated
    public void deleteProcess(final long processDefinitionId) throws SBonitaException, BonitaHomeNotSetException {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final DeleteProcess deleteProcess = new DeleteProcess(getTenantAccessor(), processDefinitionId);
        deleteProcess.execute();

        final String processesFolder = BonitaHomeServer.getInstance().getProcessesFolder(tenantAccessor.getTenantId());
        final File file = new File(processesFolder);
        if (!file.exists()) {
            file.mkdir();
        }
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
        final TechnicalLoggerService logger = tenantAccessor.getTechnicalLoggerService();
        final ClassLoaderService classLoaderService = tenantAccessor.getClassLoaderService();
        final TransactionContent transactionContent = new DisableProcess(processDefinitionService, processId, eventInstanceService, schedulerService, logger,
                SessionInfos.getUserNameFromSession(), classLoaderService);
        transactionContent.execute();
    }

}
