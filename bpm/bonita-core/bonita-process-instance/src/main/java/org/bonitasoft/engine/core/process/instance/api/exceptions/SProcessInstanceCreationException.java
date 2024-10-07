/**
 * Copyright (C) 2019 Bonitasoft S.A.
 * Bonitasoft, 32 rue Gustave Eiffel - 38000 Grenoble
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
package org.bonitasoft.engine.core.process.instance.api.exceptions;

import lombok.Getter;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.core.process.definition.model.SProcessDefinition;

/**
 * @author Baptiste Mesta
 */
public class SProcessInstanceCreationException extends SBonitaException {

    private static final long serialVersionUID = 7581906795549409593L;

    @Getter
    private long retryAfter = -1L;

    public SProcessInstanceCreationException(final Throwable cause) {
        super(cause);
    }

    public SProcessInstanceCreationException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public SProcessInstanceCreationException(final String message) {
        super(message);
    }

    public SProcessInstanceCreationException(final String message, final long retryAfter) {
        super(message);
        this.retryAfter = retryAfter;
    }

    /**
     * @param sDefinition
     *        The process definition to add on context
     * @since 6.3
     */
    public void setProcessDefinitionOnContext(final SProcessDefinition sDefinition) {
        setProcessDefinitionIdOnContext(sDefinition.getId());
        setProcessDefinitionNameOnContext(sDefinition.getName());
        setProcessDefinitionVersionOnContext(sDefinition.getVersion());
    }

}
