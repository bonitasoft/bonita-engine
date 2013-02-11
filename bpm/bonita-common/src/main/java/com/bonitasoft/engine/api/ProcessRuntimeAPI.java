/*
 * Copyright (C) 2011-2013 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
 */
package com.bonitasoft.engine.api;

import java.util.Date;

import org.bonitasoft.engine.bpm.model.ManualTaskInstance;
import org.bonitasoft.engine.bpm.model.TaskPriority;
import org.bonitasoft.engine.exception.ActivityCreationException;
import org.bonitasoft.engine.exception.ActivityExecutionErrorException;
import org.bonitasoft.engine.exception.ActivityInterruptedException;
import org.bonitasoft.engine.exception.ActivityNotFoundException;
import org.bonitasoft.engine.exception.InvalidSessionException;
import org.bonitasoft.engine.exception.ObjectDeletionException;
import org.bonitasoft.engine.exception.ObjectNotFoundException;

/**
 * @author Matthieu Chaffotte
 */
public interface ProcessRuntimeAPI extends org.bonitasoft.engine.api.ProcessRuntimeAPI {

    /**
     * Add a manual task with given human task id.
     * 
     * @param humanTaskId
     *            Identifier of the human task
     * @param taskName
     *            name of the task
     * @param assignTo
     *            a name of user that the task assigned to
     * @param description
     *            what's the task for
     * @param dueDate
     *            expected date
     * @param priority
     *            the task priority to set
     * @return the matching an instance of manual task
     * @throws InvalidSessionException
     *             Generic exception thrown if API Session is invalid, e.g session has expired.
     * @throws ActivityExecutionErrorException
     *             An unexpected error occurred while executing the user task
     * @throws ActivityInterruptedException
     *             The activity was interrupted
     * @throws ActivityCreationException
     * @throws ActivityNotFoundException
     *             errors thrown if can't find corresponding activity
     */
    ManualTaskInstance addManualUserTask(long humanTaskId, String taskName, String displayName, long assignTo, String description, Date dueDate,
            TaskPriority priority) throws InvalidSessionException, ActivityInterruptedException, ActivityExecutionErrorException, ActivityCreationException,
            ActivityNotFoundException;

    void deleteManualUserTask(final long manualTaskId) throws InvalidSessionException, ObjectDeletionException, ObjectNotFoundException;
}
