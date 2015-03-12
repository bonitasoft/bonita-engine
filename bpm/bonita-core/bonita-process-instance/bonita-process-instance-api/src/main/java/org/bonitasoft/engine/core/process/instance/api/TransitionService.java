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
package org.bonitasoft.engine.core.process.instance.api;

import java.util.List;

import org.bonitasoft.engine.core.process.definition.model.STransitionDefinition;
import org.bonitasoft.engine.core.process.definition.model.TransitionState;
import org.bonitasoft.engine.core.process.instance.api.exceptions.STransitionCreationException;
import org.bonitasoft.engine.core.process.instance.api.exceptions.STransitionDeletionException;
import org.bonitasoft.engine.core.process.instance.model.SFlowNodeInstance;
import org.bonitasoft.engine.core.process.instance.model.archive.SATransitionInstance;
import org.bonitasoft.engine.persistence.QueryOptions;
import org.bonitasoft.engine.persistence.SBonitaReadException;

/**
 * @author Zhao Na
 * @author Matthieu Chaffotte
 * @since 6.0
 */
public interface TransitionService {

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
     * @param queryOptions
     * @return
     * @throws SBonitaReadException
     */
    List<SATransitionInstance> searchArchivedTransitionInstances(QueryOptions queryOptions) throws SBonitaReadException;

    /**
     * @param countOptions
     * @return
     * @throws SBonitaReadException
     */
    long getNumberOfArchivedTransitionInstances(QueryOptions countOptions) throws SBonitaReadException;

    /**
     * @param saTransitionInstance
     */
    void delete(SATransitionInstance saTransitionInstance) throws STransitionDeletionException;

    /**
     * Deleted all archived transitions for a specified process instance
     * 
     * @param processInstanceId
     * @throws STransitionDeletionException
     * @throws SBonitaReadException
     * @since 6.1
     */
    void deleteArchivedTransitionsOfProcessInstance(long processInstanceId) throws STransitionDeletionException, SBonitaReadException;

}
