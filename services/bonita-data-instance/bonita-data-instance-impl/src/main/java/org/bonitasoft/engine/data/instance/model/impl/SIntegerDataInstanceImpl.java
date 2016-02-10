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
package org.bonitasoft.engine.data.instance.model.impl;

import java.io.Serializable;

import org.bonitasoft.engine.data.definition.model.SDataDefinition;

/**
 * @author Zhao Na
 * @author Matthieu Chaffotte
 */
public class SIntegerDataInstanceImpl extends SDataInstanceImpl {

    private static final long serialVersionUID = 4369510522836874048L;

    private Integer value;

    public SIntegerDataInstanceImpl() {
        super();
    }

    public SIntegerDataInstanceImpl(final SDataDefinition dataDefinition) {
        super(dataDefinition);
    }

    @Override
    public Integer getValue() {
        return value;
    }

    @Override
    public void setValue(final Serializable value) {
        this.value = (Integer) value;
    }

    @Override
    public String getDiscriminator() {
        return SIntegerDataInstanceImpl.class.getSimpleName();
    }

}
