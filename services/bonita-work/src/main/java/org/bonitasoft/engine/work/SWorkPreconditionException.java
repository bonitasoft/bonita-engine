package org.bonitasoft.engine.work;

import org.bonitasoft.engine.commons.exceptions.SBonitaException;

/**
 * Happens when a work could not be executed because its preconditions where not met.
 * e.g. NotifyChildFinishWork is executed on an unexisting or not finished flow node
 *
 */
public class SWorkPreconditionException extends SBonitaException {
    public SWorkPreconditionException(String message, Exception cause) {
        super(message, cause);
    }

    public SWorkPreconditionException(String message) {
        super(message);
    }
}
