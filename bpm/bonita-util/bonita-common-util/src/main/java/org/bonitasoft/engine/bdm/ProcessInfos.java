/*
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
 */

package org.bonitasoft.engine.bdm;


import org.bonitasoft.engine.core.operation.model.SOperation;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author mazourd
 */
public class ProcessInfos {

    Map<String, Serializable> processInputs = null;

    List<SOperation> operations = null;

    Map<String, Object> context = null;

    long starterId = 0;

    long starterSubstituteId = 0;

    public ProcessInfos() {
    }

    public ProcessInfos(Map<String, Serializable> processInputs, List<SOperation> operations, Map<String, Object> context, long starterId,
            long starterSubstituteId) {
        this.context = context;
        this.operations = operations;
        this.processInputs = processInputs;
        this.starterId = starterId;
        this.starterSubstituteId = starterSubstituteId;

    }

    public ProcessInfos(List<SOperation> operations) {
        this.operations = operations;
    }

    public ProcessInfos(Map<String, Serializable> processInputs, List<SOperation> operations) {
        this.processInputs = processInputs;
        this.operations = operations;
    }

    public ProcessInfos(List<SOperation> operations, Map<String, Object> context, long starterId, long starterSubstituteId) {
        this.operations = operations;
        this.context = context;
        this.starterId = starterId;
        this.starterSubstituteId = starterSubstituteId;
    }

    public Map<String, Serializable> getProcessInputs() {
        return processInputs;
    }

    public void setProcessInputs(Map<String, Serializable> processInputs) {
        this.processInputs = processInputs;
    }

    public List<SOperation> getOperations() {
        return operations;
    }

    public void setOperations(List<SOperation> operations) {
        this.operations = operations;
    }

    public Map<String, Object> getContext() {
        return context;
    }

    public void setContext(Map<String, Object> context) {
        this.context = context;
    }

    public long getStarterId() {
        return starterId;
    }

    public void setStarterId(long starterId) {
        this.starterId = starterId;
    }

    public long getStarterSubstituteId() {
        return starterSubstituteId;
    }

    public void setStarterSubstituteId(long starterSubstituteId) {
        this.starterSubstituteId = starterSubstituteId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProcessInfos that = (ProcessInfos) o;
        return Objects.equals(starterId, that.starterId) &&
                Objects.equals(starterSubstituteId, that.starterSubstituteId) &&
                Objects.equals(processInputs, that.processInputs) &&
                Objects.equals(operations, that.operations) &&
                Objects.equals(context, that.context);
    }

    @Override
    public int hashCode() {
        return Objects.hash(processInputs, operations, context, starterId, starterSubstituteId);
    }
}
