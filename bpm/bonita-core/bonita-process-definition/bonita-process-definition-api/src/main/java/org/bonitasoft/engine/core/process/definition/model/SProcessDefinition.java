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
 */
package org.bonitasoft.engine.core.process.definition.model;

import java.util.List;
import java.util.Set;

import org.bonitasoft.engine.expression.model.SExpression;

/**
 * @author Elias Ricken de Medeiros
 * @author Matthieu Chaffotte
 * @author Celine Souchet
 */
public interface SProcessDefinition extends SNamedElement {

    String getVersion();

    String getDescription();

    SFlowElementContainerDefinition getProcessContainer();

    SActorDefinition getActorInitiator();

    Set<SActorDefinition> getActors();

    String getStringIndexLabel(int index);

    SExpression getStringIndexValue(int index);

    Set<SParameterDefinition> getParameters();

    SParameterDefinition getParameter(String parameterName);

    boolean hasConnectors();

    SContractDefinition getContract();

    List<SContextEntry> getContext();
}
