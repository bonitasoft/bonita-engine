package org.bonitasoft.engine.commons;

import org.bonitasoft.engine.commons.exceptions.SBonitaException;

/**
 * @author Baptiste Mesta
 */
public interface LifecycleService {

    /**
     * Start the service
     * 
     * @throws SBonitaException
     */
    public void start() throws SBonitaException;

    public void stop() throws SBonitaException;

    /**
     * Temporary halt the execution of this service.
     */
    void pause() throws SBonitaException;

    /**
     * resume the execution the service
     */

    void resume() throws SBonitaException;

}
