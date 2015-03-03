/**
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
 * This library is free software; you can redistribute it and/or modify it under the terms
 * of the GNU Lesser General Public License as published by the Free Software Foundation
 * version 2.1 of the License.
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License along with this
 * program; if not, write to the Free Software Foundation, Inc., 51 Franklin Street, Fifth
 * Floor, Boston, MA 02110-1301, USA.
 **/
package org.bonitasoft.engine.api;

import org.bonitasoft.engine.exception.CreationException;
import org.bonitasoft.engine.exception.DeletionException;
import org.bonitasoft.engine.exception.UpdateException;
import org.bonitasoft.engine.platform.Platform;
import org.bonitasoft.engine.platform.PlatformNotFoundException;
import org.bonitasoft.engine.platform.PlatformState;
import org.bonitasoft.engine.platform.StartNodeException;
import org.bonitasoft.engine.platform.StopNodeException;
import org.bonitasoft.engine.session.InvalidSessionException;

/**
 * <b>Manage the platform.</b>
 * <p>
 * The platform is the base on which runs the engine.<br>
 * It mainly handles the creation of tables in database and also allow to start/stop a Node which is the current Virtual machine on which runs the engine. There
 * is only one platform for a running Bonita Engine.
 * </p>
 *
 * @author Elias Ricken de Medeiros
 * @author Lu Kai
 * @author Zhang Bole
 * @author Matthieu Chaffotte
 * @author Emmanuel Duchastenier
 */
public interface PlatformAPI {

    /**
     * <b>Create the platform.</b>
     * <p>
     * The platform creation is done in 3 steps:
     * <ul>
     * <li>Creation of the persistence structure: tables are created</li>
     * <li>Initialization of persistence structure: index are added, default values and so on</li>
     * <li>The platform state is persisted</li>
     * </ul>
     *
     * @throws org.bonitasoft.engine.session.InvalidSessionException
     *         occurs if the API session is invalid, e.g session has expired.
     * @throws CreationException
     *         occurs when an exception is thrown during platform creation
     */
    void createPlatform() throws CreationException;

    /**
     * <b>Initialize the platform.</b>
     * The running environment of Bonita Engine is initialized and marked as activated.<br>
     * Business elements linked to the execution are initialized, after this step the technical user will be able to connect to the engine and to import the
     * organization. No user are created by default and the default username/password to connect to the Bonita Engine are stored in the file bonita-platform.properties
     * that is in the Bonita home.
     *
     * @throws org.bonitasoft.engine.session.InvalidSessionException
     *         occurs if the API session is invalid, e.g session has expired.
     * @throws CreationException
     *         occurs when an exception is thrown during platform creation
     */
    void initializePlatform() throws CreationException;

    /**
     * Utilitary method that call {@link #createPlatform()} and {@link #initializePlatform()}
     *
     * @see #createPlatform()
     * @see #initializePlatform()
     * @throws org.bonitasoft.engine.session.InvalidSessionException
     *         occurs if the API session is invalid, e.g session has expired.
     * @throws CreationException
     *         occurs when an exception is thrown during platform creation
     */
    void createAndInitializePlatform() throws CreationException;

    /**
     * <b>Starts the node.</b>
     * <p>
     * The node is the currently Java Virtual Machine on which Bonita Engine is running
     * <p>
     * Starting the node make the Scheduler service to start and restart elements that were not finished by the Work service on the previous shutdown.
     *
     * @throws org.bonitasoft.engine.session.InvalidSessionException
     *         occurs if API Session is invalid, e.g session has expired.
     * @throws StartNodeException
     *         occurs when an exception is thrown during the activation of the node
     */
    void startNode() throws StartNodeException;

    /**
     * <b>Stops the node.</b>
     * <p>
     * The node is the currently Java Virtual Machine on which Bonita Engine is running
     * <p>
     * Stopping the node make the Scheduler service to stop.
     *
     * @throws org.bonitasoft.engine.session.InvalidSessionException
     *         occurs if API Session is invalid, e.g session has expired.
     * @throws StopNodeException
     *         occurs when an exception is thrown during the stop of the node
     */
    void stopNode() throws StopNodeException;

    /**
     * <b>Clean the platform.</b>
     * <p>
     * Empty all execution informations, i.e. database tables are cleaned and a new execution environment can be initialized.
     * <p>
     * /!\Please remember that <b>all data will be DELETED</b>/!\
     * <p>
     * This method does the opposite of {@link #initializePlatform()}
     *
     * @see #initializePlatform()
     * @throws org.bonitasoft.engine.session.InvalidSessionException
     *         Generic exception thrown if API Session is invalid, e.g session has expired.
     * @throws DeletionException
     *         occurs when an exception is thrown during platform deletion
     */
    void cleanPlatform() throws DeletionException;

    /**
     * <b>Delete the platform</b>
     * <p>
     * This method delete the platform, i.e. all the database tables.
     *
     * @see #createPlatform()
     * @throws org.bonitasoft.engine.session.InvalidSessionException
     *         Generic exception thrown if API Session is invalid, e.g session has expired.
     * @throws DeletionException
     *         occurs when an exception is thrown during platform deletion
     */
    void deletePlatform() throws DeletionException;

    /**
     * Clean and delete a platform.
     *
     * @see #cleanPlatform()
     * @see #deletePlatform()
     * @throws org.bonitasoft.engine.session.InvalidSessionException
     *         Generic exception thrown if API Session is invalid, e.g session has expired.
     * @throws DeletionException
     *         occurs when an exception is thrown during platform deletion
     * @deprecated since 6.5.0 (typo in method name). Use {@link #cleanAndDeletePlatform()} instead.
     */
    @Deprecated
    void cleanAndDeletePlaftorm() throws DeletionException;

    /**
     * Clean and delete a platform.
     *
     * @see #cleanPlatform()
     * @see #deletePlatform()
     * @throws InvalidSessionException
     *         Generic exception thrown if API Session is invalid, e.g session has expired.
     * @throws DeletionException
     *         occurs when an exception is thrown during platform deletion
     */
    void cleanAndDeletePlatform() throws DeletionException;

    /**
     * Get the platform.
     *
     * @return the Platform object
     * @throws org.bonitasoft.engine.session.InvalidSessionException
     *         Generic exception thrown if API Session is invalid, e.g session has expired.
     * @throws PlatformNotFoundException
     *         occurs when the identifier does not refer to an existing platform
     */
    Platform getPlatform() throws PlatformNotFoundException;

    /**
     * Check if the platform created or not.
     *
     * @return true if the platform existed
     * @throws org.bonitasoft.engine.session.InvalidSessionException
     *         Generic exception thrown if API Session is invalid, e.g session has expired.
     * @throws PlatformNotFoundException
     *         occurs when the identifier does not refer to an existing platform
     */
    boolean isPlatformCreated() throws PlatformNotFoundException;

    /**
     * Get the state of the platform of the current node
     *
     * @return {@link PlatformState#STARTED} or {@link PlatformState#STOPPED} depending on the scheduler state
     * @throws org.bonitasoft.engine.session.InvalidSessionException
     *         Generic exception thrown if API Session is invalid, e.g session has expired.
     * @throws PlatformNotFoundException
     *         occurs when the identifier does not refer to an existing platform
     */
    PlatformState getPlatformState() throws PlatformNotFoundException;

    /**
     * Is the current node started?
     *
     * @return true if the node is started, false if not started or if its state cannot be determined.
     * @since 6.1
     */
    boolean isNodeStarted();

    /**
     * Reschedules triggers which are in error state.
     *
     * @throws UpdateException
     *         If an exception occurs during the scheduling
     * @since 6.2
     */
    void rescheduleErroneousTriggers() throws UpdateException;

}
