/**
 * Copyright (C) 2011-2013 BonitaSoft S.A.
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
package org.bonitasoft.engine.core.process.instance.api;

import java.util.List;

import org.bonitasoft.engine.core.process.definition.model.STransitionDefinition;
import org.bonitasoft.engine.core.process.definition.model.TransitionState;
import org.bonitasoft.engine.core.process.instance.api.exceptions.STransitionCreationException;
import org.bonitasoft.engine.core.process.instance.api.exceptions.STransitionDeletionException;
import org.bonitasoft.engine.core.process.instance.api.exceptions.STransitionInstanceNotFoundException;
import org.bonitasoft.engine.core.process.instance.api.exceptions.STransitionModificationException;
import org.bonitasoft.engine.core.process.instance.api.exceptions.STransitionReadException;
import org.bonitasoft.engine.core.process.instance.model.SFlowNodeInstance;
import org.bonitasoft.engine.core.process.instance.model.STransitionInstance;
import org.bonitasoft.engine.core.process.instance.model.archive.SATransitionInstance;
import org.bonitasoft.engine.persistence.QueryOptions;
import org.bonitasoft.engine.persistence.SBonitaSearchException;

/**
 * @author Zhao Na
 * @author Matthieu Chaffotte
 * @since 6.0
 */
public interface TransitionService {

    String TRANSITIONINSTANCE = "TRANSITIONINSTANCE";

    String TRANSITIONINSTANCE_STATE = "TRANSITIONINSTANCE_STATE";

    /**
     * Create transitionInstance in DB according to the given transitionInstance object
     * 
     * @param transitionInstance
     *            the transitionInstance object
     * @throws STransitionCreationException
     */
    void create(STransitionInstance transitionInstance) throws STransitionCreationException;

    /**
     * Retrieve transition by its id
     * 
     * @param transitionId
     *            identifier of transition instance
     * @return the transition instance object has id corresponding to the parameter
     * @throws STransitionReadException
     * @throws STransitionInstanceNotFoundException
     */
    STransitionInstance get(long transitionId) throws STransitionReadException, STransitionInstanceNotFoundException;

    /**
     * Verify if a container contains Actived transition or not.
     * 
     * @param parentContainerId
     *            identifier of parent container
     * @return true if contains; false otherwise
     * @throws STransitionReadException
     */
    boolean containsActiveTransition(long parentContainerId) throws STransitionReadException;

    /**
     * Get the number of transition instances according to countOptions
     * 
     * @param countOptions
     *            count parameters
     * @return the number of transitions instances according to countOptions
     * @throws SBonitaSearchException
     */
    long getNumberOfTransitionInstances(QueryOptions countOptions) throws SBonitaSearchException;

    /**
     * Retrieve transition instances according to searchOptions
     * 
     * @param searchOptions
     *            define filters, range and order by options
     * @return
     * @throws SBonitaSearchException
     *             if a problem is found during the search
     */
    List<STransitionInstance> search(QueryOptions searchOptions) throws SBonitaSearchException;

    /**
     * Archive a transition from its instance
     * 
     * @param sTransitionInstance
     *            the transition instance to archive
     * @param sFlowNodeInstanceId
     *            the target flow node instance from this transition
     * @param transitionState
     *            the state of the transition
     * @throws STransitionCreationException
     */
    void archive(STransitionInstance sTransitionInstance, long sFlowNodeInstanceId, TransitionState transitionState) throws STransitionCreationException;

    /**
     * Archive a Transition from its definition and state
     * 
     * @param sTransitionDefinition
     *            the transition to archive
     * @param sFlowNodeInstance
     *            source flow node of the transition to archive
     * @param transitionState
     *            the state to set
     */
    void archive(STransitionDefinition sTransitionDefinition, SFlowNodeInstance sFlowNodeInstance, TransitionState transitionState)
            throws STransitionCreationException;

    /**
     * Delete the transition instance
     * 
     * @param transitionInstance
     * @throws STransitionModificationException
     * @since 6.0
     */
    void delete(STransitionInstance transitionInstance) throws STransitionDeletionException;

    /**
     * @param searchOptions
     * @return
     * @throws SBonitaSearchException
     */
    List<SATransitionInstance> searchArchived(QueryOptions searchOptions) throws SBonitaSearchException;

    /**
     * @param countOptions
     * @return
     * @throws SBonitaSearchException
     */
    long getNumberOfArchivedTransitionInstances(QueryOptions countOptions) throws SBonitaSearchException;

    /**
     * @param processInstanceId
     * @return
     * @throws STransitionReadException
     */
    List<SATransitionInstance> getArchivedTransitionOfProcessInstance(long processInstanceId, int from, int numberOfResult) throws STransitionReadException;

    /**
     * @param saTransitionInstance
     */
    void delete(SATransitionInstance saTransitionInstance) throws STransitionDeletionException;

    /**
     * Deleted all archived transitions for a specified process instance
     * 
     * @param processInstanceId
     * @throws STransitionDeletionException
     * @throws STransitionReadException
     * @since 6.1
     */
    void deleteArchivedTransitionsOfProcessInstance(long processInstanceId) throws STransitionDeletionException, STransitionReadException;

}
