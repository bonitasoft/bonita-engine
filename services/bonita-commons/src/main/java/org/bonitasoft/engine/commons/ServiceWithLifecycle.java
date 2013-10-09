package org.bonitasoft.engine.commons;

import java.util.concurrent.TimeoutException;

import org.bonitasoft.engine.commons.exceptions.SBonitaException;

/**
 * 
 * 
 * @author Baptiste Mesta
 * 
 */
public interface ServiceWithLifecycle {

    /**
     * 
     * Start the service
     * 
     * @throws SBonitaException
     */
    public void start() throws SBonitaException;

    public void stop() throws SBonitaException, TimeoutException;

}
