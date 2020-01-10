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
package org.bonitasoft.engine.core.process.instance.model.builder.impl;

import org.bonitasoft.engine.core.process.instance.model.SMultiInstanceActivityInstance;
import org.bonitasoft.engine.core.process.instance.model.builder.SMultiInstanceActivityInstanceBuilder;

/**
 * @author Baptiste Mesta
 * @author Celine Souchet
 */
public class SMultiInstanceActivityInstanceBuilderImpl extends SActivityInstanceBuilderImpl
        implements SMultiInstanceActivityInstanceBuilder {

    public SMultiInstanceActivityInstanceBuilderImpl(final SMultiInstanceActivityInstance entity) {
        super(entity);
    }

    @Override
    public SMultiInstanceActivityInstance done() {
        return (SMultiInstanceActivityInstance) entity;
    }

    @Override
    public SMultiInstanceActivityInstanceBuilder setLoopDataInputRef(final String loopDataInputRef) {
        ((SMultiInstanceActivityInstance) entity).setLoopDataInputRef(loopDataInputRef);
        return this;
    }

    @Override
    public SMultiInstanceActivityInstanceBuilder setLoopDataOutputRef(final String loopDataOutputRef) {
        ((SMultiInstanceActivityInstance) entity).setLoopDataOutputRef(loopDataOutputRef);
        return this;
    }

    @Override
    public SMultiInstanceActivityInstanceBuilder setDataInputItemRef(final String dataInputItemRef) {
        ((SMultiInstanceActivityInstance) entity).setDataInputItemRef(dataInputItemRef);
        return this;
    }

    @Override
    public SMultiInstanceActivityInstanceBuilder setDataOutputItemRef(final String dataOutputItemRef) {
        ((SMultiInstanceActivityInstance) entity).setDataOutputItemRef(dataOutputItemRef);
        return this;
    }

}
