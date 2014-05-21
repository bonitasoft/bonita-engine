/**
 * Copyright (C) 2012 BonitaSoft S.A.
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
package org.bonitasoft.engine.api.impl.transaction.actor;

import java.util.ArrayList;
import java.util.List;

import org.bonitasoft.engine.actor.mapping.ActorMappingService;
import org.bonitasoft.engine.actor.mapping.model.SActor;
import org.bonitasoft.engine.actor.mapping.model.SActorMember;
import org.bonitasoft.engine.bpm.process.Problem;
import org.bonitasoft.engine.bpm.process.Problem.Level;
import org.bonitasoft.engine.bpm.process.impl.internal.ProblemImpl;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.commons.transaction.TransactionContent;

/**
 * @author Matthieu Chaffotte
 */
public class CheckActorMappingList implements TransactionContent {

    private final ActorMappingService actorMappingService;

    private final long processDefinitionId;

    private List<Problem> problems;

    public CheckActorMappingList(final ActorMappingService actorMappingService, final long processDefinitionId) {
        this.actorMappingService = actorMappingService;
        this.processDefinitionId = processDefinitionId;
    }

    @Override
    public void execute() throws SBonitaException {
        final List<SActor> actors = actorMappingService.getActors(processDefinitionId);
        problems = new ArrayList<Problem>();
        for (final SActor sActor : actors) {
            final List<SActorMember> actorMembers = actorMappingService.getActorMembers(sActor.getId(), 0, 1);
            if (actorMembers.isEmpty()) {
                final Problem problem = new ProblemImpl(Level.ERROR, sActor.getId(), "actor", "Actor '" + sActor.getName() + "' does not contain any members");
                problems.add(problem);
            }
        }
    }

    public List<Problem> getProblems() {
        return problems;
    }

}
