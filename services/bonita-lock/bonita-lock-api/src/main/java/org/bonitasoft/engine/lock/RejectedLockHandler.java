package org.bonitasoft.engine.lock;

public interface RejectedLockHandler {

	void executeOnLockFree() throws SLockException;
	
}
