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

import org.bonitasoft.engine.core.process.definition.model.SNamedElement;

/**
 * @author Matthieu Chaffotte
 */
public class SNamedElementImpl extends SBaseElementImpl implements SNamedElement {

    private static final long serialVersionUID = 4789196762554891321L;

    private final String name;

    public SNamedElementImpl(final String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + (name == null ? 0 : name.hashCode());
        return result;
    }

    protected EQUALS_STATE checkFurtherNaiveEquality(final Object obj) {
        EQUALS_STATE state;
        if ((state = super.checkNaiveEquality(obj)) != EQUALS_STATE.CONTINUE) {
            return state;
        } else if (!super.equals(obj)) {
            return EQUALS_STATE.RETURN_FALSE;
        } else {
            return EQUALS_STATE.CONTINUE;
        }
    }

    @Override
    public boolean equals(final Object obj) {
        switch (checkFurtherNaiveEquality(obj)) {
            case RETURN_FALSE:
                return false;
            case RETURN_TRUE:
                return true;
            case CONTINUE:
            default:
                break;
        }
        final SNamedElementImpl other = (SNamedElementImpl) obj;
        if (name == null) {
            if (other.name != null) {
                return false;
            }
        } else if (!name.equals(other.name)) {
            return false;
        }
        return true;
    }

}
