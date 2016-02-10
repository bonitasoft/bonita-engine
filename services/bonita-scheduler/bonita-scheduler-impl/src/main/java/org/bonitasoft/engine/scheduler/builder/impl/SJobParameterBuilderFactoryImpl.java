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
package org.bonitasoft.engine.scheduler.builder.impl;

import java.io.Serializable;

import org.bonitasoft.engine.scheduler.builder.SJobParameterBuilder;
import org.bonitasoft.engine.scheduler.builder.SJobParameterBuilderFactory;
import org.bonitasoft.engine.scheduler.model.impl.SJobParameterImpl;

public class SJobParameterBuilderFactoryImpl implements SJobParameterBuilderFactory {

    private static final String JOB_DESCRIPTOR_ID = "jobDescriptorId";

    private static final String KEY = "key";

    private static final String VALUE = "value";

    @Override
    public SJobParameterBuilder createNewInstance(final String key, final Serializable value) {
        final SJobParameterImpl entity = new SJobParameterImpl(key, value);
        return new SJobParameterBuilderImpl(entity);
    }

    @Override
    public String getJobDescriptorIdKey() {
        return JOB_DESCRIPTOR_ID;
    }

    @Override
    public String getKeyKey() {
        return KEY;
    }

    @Override
    public String getValueKey() {
        return VALUE;
    }

}
