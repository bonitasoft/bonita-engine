package org.bonitasoft.engine.execution.work;

import org.bonitasoft.engine.core.process.instance.model.SFlowElementsContainerType;
import org.bonitasoft.engine.execution.RescheduleWorkRejectedLockHandler;
import org.bonitasoft.engine.lock.BonitaLock;
import org.bonitasoft.engine.lock.LockService;
import org.bonitasoft.engine.lock.RejectedLockHandler;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.work.WorkService;

public abstract class TxLockProcessInstanceWork extends TxBonitaWork {

    private static final long serialVersionUID = -4604852239659029393L;
    
	protected final long processInstanceId;
	private transient LockService lockService;
	private transient BonitaLock lock;

	public TxLockProcessInstanceWork(long processInstanceId) {
		super();
		this.processInstanceId = processInstanceId;

	}

	@Override
	protected boolean preWork() throws Exception {
		this.lockService = getTenantAccessor().getLockService();
		final WorkService workService = getTenantAccessor().getWorkService();
		final TechnicalLoggerService logger = getTenantAccessor().getTechnicalLoggerService();
		final String objectType = SFlowElementsContainerType.PROCESS.name();
		
    	final RejectedLockHandler handler = new RescheduleWorkRejectedLockHandler(logger, workService, this);
    	
        this.lock = lockService.tryLock(processInstanceId, objectType, handler);
        if (lock == null) {
            return false;
        }
        return true;
	}

	@Override
	protected void afterWork() throws Exception {
		lockService.unlock(lock);
	}
}
