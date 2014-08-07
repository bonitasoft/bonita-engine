/**
 * Copyright (C) 2014 BonitaSoft S.A.
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
package org.bonitasoft.engine.bpm.contract.impl;

import org.bonitasoft.engine.bpm.contract.InputDefinition;
import org.bonitasoft.engine.bpm.internal.DescriptionElementImpl;

/**
 * @author Matthieu Chaffotte
 */
public class InputDefinitionImpl extends DescriptionElementImpl implements InputDefinition {

    private static final long serialVersionUID = 2836592506382887928L;

    private final String type;

    public InputDefinitionImpl(final String name, final String type, final String description) {
        super(name, description);
        this.type = type;
    }

    @Override
    public String getType() {
        return type;
    }

}
