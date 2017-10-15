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
package org.bonitasoft.engine.data.instance.model.archive.impl;

import java.io.Serializable;

import org.bonitasoft.engine.data.instance.model.SDataInstance;
import org.bonitasoft.engine.data.instance.model.archive.SAFloatDataInstance;

/**
 * @author Celine Souchet
 * @author Matthieu Chaffotte
 */
public class SAFloatDataInstanceImpl extends SADataInstanceImpl implements SAFloatDataInstance {

    private static final long serialVersionUID = -6859822521894224067L;

    private Float value;

    public SAFloatDataInstanceImpl() {
        super();
    }

    public SAFloatDataInstanceImpl(final SDataInstance sDataInstance) {
        super(sDataInstance);
        value = (Float) sDataInstance.getValue();
    }

    @Override
    public String getDiscriminator() {
        return SAFloatDataInstanceImpl.class.getSimpleName();
    }

    @Override
    public Float getValue() {
        return value;
    }

    @Override
    public void setValue(final Serializable value) {
        this.value = (Float) value;
    }

}
