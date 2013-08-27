package org.bonitasoft.engine.execution;

import org.bonitasoft.engine.lock.RejectedLockHandler;
import org.bonitasoft.engine.lock.SLockException;
import org.bonitasoft.engine.log.technical.TechnicalLogSeverity;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.work.BonitaWork;
import org.bonitasoft.engine.work.WorkRegisterException;
import org.bonitasoft.engine.work.WorkService;

public class RescheduleWorkRejectedLockHandler implements RejectedLockHandler {

	private final TechnicalLoggerService logger;
	private final WorkService workService;
	private final BonitaWork work;
	
	
	public RescheduleWorkRejectedLockHandler(final TechnicalLoggerService logger, final WorkService workService, final BonitaWork work) {
	    super();
	    this.logger = logger;
	    this.workService = workService;
	    this.work = work;
    }

	@Override
	public void executeOnLockFree() throws SLockException {
		if (logger.isLoggable(getClass(), TechnicalLogSeverity.DEBUG)) {
			logger.log(this.getClass(), TechnicalLogSeverity.DEBUG, "Failed to lock, the work will be rescheduled: " + work.getDescription());
		}
		try {
	        workService.executeWork(work);
        } catch (WorkRegisterException e) {
	        throw new SLockException(e);
        }
	}
}
