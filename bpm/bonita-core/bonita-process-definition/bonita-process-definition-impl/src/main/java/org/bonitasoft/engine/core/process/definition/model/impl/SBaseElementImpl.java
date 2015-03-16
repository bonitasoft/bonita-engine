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
package org.bonitasoft.engine.core.process.definition.model.impl;

import org.bonitasoft.engine.core.process.definition.model.SBaseElement;

/**
 * @author Matthieu Chaffotte
 */
public class SBaseElementImpl implements SBaseElement {

    private static final long serialVersionUID = -2401748455848222695L;

    private Long id;

    protected static enum EQUALS_STATE {
        CONTINUE, RETURN_FALSE, RETURN_TRUE
    }

    @Override
    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (id == null ? 0 : id.hashCode());
        return result;
    }

    protected EQUALS_STATE checkNaiveEquality(final Object obj) {
        if (this == obj) {
            return EQUALS_STATE.RETURN_TRUE;
        } else if (obj == null) {
            return EQUALS_STATE.RETURN_FALSE;
        } else if (getClass() != obj.getClass()) {
            return EQUALS_STATE.RETURN_FALSE;
        } else {
            return EQUALS_STATE.CONTINUE;
        }
    }

    @Override
    public boolean equals(final Object obj) {
        switch (checkNaiveEquality(obj)) {
            case RETURN_FALSE:
                return false;
            case RETURN_TRUE:
                return true;
            case CONTINUE:
            default:
                break;
        }
        final SBaseElementImpl other = (SBaseElementImpl) obj;
        if (id == null) {
            if (other.id != null) {
                return false;
            }
        } else if (!id.equals(other.id)) {
            return false;
        }
        return true;
    }

}
