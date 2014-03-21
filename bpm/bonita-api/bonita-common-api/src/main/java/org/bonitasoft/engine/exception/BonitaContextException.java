package org.bonitasoft.engine.exception;

/**
 * @author Aurelien Pupier
 *
 */
public interface BonitaContextException {

	public abstract long getTenantId();

	public abstract void setTenantId(long tenantId);

	public abstract String getHostname();

	public abstract void setHostname(String hostname);

	public abstract String getUserName();

	public abstract void setUserName(String userName);
	
	public abstract long getThreadId();
	
	public abstract void setThreadId(long threadId);

}