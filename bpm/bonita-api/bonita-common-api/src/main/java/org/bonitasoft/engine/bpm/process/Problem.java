/**
 * Copyright (C) 2012, 2014 BonitaSoft S.A.
 * BonitaSoft, 31 rue Gustave Eiffel - 38000 Grenoble
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
package org.bonitasoft.engine.bpm.process;

import java.io.Serializable;

/**
 * A <code>Problem</code> explains the issue in the {@link DesignProcessDefinition} when it is not well designed.
 * It can concern :
 * <ul>
 * <li>structural problems like a {@link BoundaryEventDefinition} without an exception flow,</li>
 * <li>naming problems like two {@link FlowNodeDefinition}s with the same name,</li>
 * <li>type problems like an {@link Expression} which has not the right type,</li>
 * <li>...</li>
 * </ul>
 *
 * @author Matthieu Chaffotte
 * @author Celine Souchet
 * @version 6.3.5
 * @since 6.0.0
 */
public interface Problem extends Serializable {

    /**
     * Defines a severity level.
     *
     * @author Matthieu Chaffotte
     */
    public enum Level {
        /**
         * The {@link DesignProcessDefinition} cannot be deployed.
         */
        ERROR,
        /**
         * The {@link DesignProcessDefinition} can be deployed and used but contains something that should be updated.
         */
        WARNING,
        /**
         * Not currently used.
         */
        INFO
    }

    /**
     * Returns the severity of the problem.
     *
     * @return the severity
     */
    Level getLevel();

    /**
     * Returns the resource/concept name of the problem.
     * It can be related to {@link DataDefinition}, {@link FlowNodeDefinition}, ...
     *
     * @return the resource name
     */
    String getResource();

    /**
     * Returns the resource identifier.
     * Generally, it is the name of the entity that has the problem in the {@link DesignProcessDefinition}.
     * For example, the name of the {@link UserTaskDefinition}.
     *
     * @return the resource identifier
     */
    String getResourceId();

    /**
     * Returns the description/explanation of the problem.
     *
     * @return the explanation of the problem
     */
    String getDescription();

}
