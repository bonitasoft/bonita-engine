/**
 * Copyright (C) 2011 BonitaSoft S.A.
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
package org.bonitasoft.engine.scheduler.impl.model;

import java.io.Serializable;

import org.bonitasoft.engine.scheduler.JobParameterBuilder;
import org.bonitasoft.engine.scheduler.SJobParameter;

public class JobParameterBuilderImpl implements JobParameterBuilder {

    private static final String JOB_DESCRIPTOR_ID = "jobDescriptorId";

    private static final String KEY = "key";

    private static final String VALUE = "value";

    private SJobParameterImpl entity;

    @Override
    public JobParameterBuilder createNewInstance(final String key, final Serializable value) {
        entity = new SJobParameterImpl(key, value);
        return this;
    }

    @Override
    public SJobParameter done() {
        return entity;
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

    @Override
    public JobParameterBuilder setJobDescriptorId(final long jobDescriptorId) {
        entity.setJobDescriptorId(jobDescriptorId);
        return this;
    }

}
