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

import org.bonitasoft.engine.scheduler.SJobParameter;

public class SJobParameterImpl extends SPersistentObjectImpl implements SJobParameter {

    private static final long serialVersionUID = 3170527651672434929L;

    private long jobDescriptorId;

    private String key;

    private Serializable value;

    public SJobParameterImpl() {
    }

    public SJobParameterImpl(final String key, final Serializable value) {
        this.key = key;
        this.value = value;
    }

    @Override
    public String getDiscriminator() {
        return SJobParameterImpl.class.getName();
    }

    public long getJobDescriptorId() {
        return jobDescriptorId;
    }

    @Override
    public String getKey() {
        return key;
    }

    @Override
    public long getSJobDescriptorId() {
        return getJobDescriptorId();
    }

    @Override
    public Serializable getValue() {
        return value;
    }

    public void setJobDescriptorId(final long jobDescriptorId) {
        this.jobDescriptorId = jobDescriptorId;
    }

    public void setKey(final String key) {
        this.key = key;
    }

    public void setValue(final Serializable value) {
        this.value = value;
    }

}
