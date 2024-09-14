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
package org.bonitasoft.engine.work;

import static org.apache.commons.lang3.builder.ToStringStyle.SHORT_PREFIX_STYLE;

import java.io.Serializable;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * @author Baptiste Mesta.
 */
public class WorkDescriptor implements Serializable {

    private final String uuid = UUID.randomUUID().toString();
    private final String type;
    private Long tenantId;
    private final Map<String, Serializable> parameters;
    private int retryCount = 0;
    private Instant executionThreshold;
    private int executionCount = 0;
    private Instant registrationDate;
    private boolean abnormalExecutionDetected = false;

    public WorkDescriptor(String type) {
        this.type = type;
        this.parameters = new HashMap<>();
    }

    public Long getTenantId() {
        return tenantId;
    }

    public WorkDescriptor setTenantId(Long tenantId) {
        this.tenantId = tenantId;
        return this;
    }

    public String getUuid() {
        return uuid;
    }

    public String getType() {
        return type;
    }

    public Serializable getParameter(String key) {
        if (!parameters.containsKey(key)) {
            throw new IllegalStateException(
                    String.format("Parameter %s is not set on the work descriptor %s", key, this));
        }
        return parameters.get(key);
    }

    public Long getLong(String key) {
        return (Long) getParameter(key);
    }

    public Integer getInteger(String key) {
        return (Integer) getParameter(key);
    }

    public Boolean getBoolean(String key) {
        return (Boolean) getParameter(key);
    }

    public String getString(String key) {
        return (String) getParameter(key);
    }

    public static WorkDescriptor create(String type) {
        return new WorkDescriptor(type);
    }

    public WorkDescriptor withParameter(String key, Serializable value) {
        parameters.put(key, value);
        return this;
    }

    public Instant getExecutionThreshold() {
        return executionThreshold;
    }

    public WorkDescriptor mustBeExecutedAfter(Instant mustBeExecutedAfter) {
        this.executionThreshold = mustBeExecutedAfter;
        return this;
    }

    public int getRetryCount() {
        return retryCount;
    }

    public void incrementRetryCount() {
        retryCount++;
    }

    public int getExecutionCount() {
        return executionCount;
    }

    public void incrementExecutionCount() {
        executionCount++;
    }

    public void setRegistrationDate(Instant registrationDate) {
        this.registrationDate = registrationDate;
    }

    public Instant getRegistrationDate() {
        return registrationDate;
    }

    public WorkDescriptor abnormalExecutionDetected() {
        this.abnormalExecutionDetected = true;
        return this;
    }

    public boolean isAbnormalExecutionDetected() {
        return abnormalExecutionDetected;
    }

    public String getDescription() {
        return new ToStringBuilder(this, SHORT_PREFIX_STYLE)
                .append(type)
                .append("parameters", parameters).toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        WorkDescriptor that = (WorkDescriptor) o;
        return new EqualsBuilder()
                .append(retryCount, that.retryCount)
                .append(uuid, that.uuid)
                .append(type, that.type)
                .append(tenantId, that.tenantId)
                .append(parameters, that.parameters)
                .append(executionThreshold, that.executionThreshold)
                .append(executionCount, that.executionCount)
                .append(registrationDate, that.registrationDate)
                .append(abnormalExecutionDetected, that.abnormalExecutionDetected)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(uuid)
                .append(type)
                .append(tenantId)
                .append(parameters)
                .append(retryCount)
                .append(executionThreshold)
                .append(executionCount)
                .append(registrationDate)
                .append(abnormalExecutionDetected)
                .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, SHORT_PREFIX_STYLE)
                .append("uuid", uuid)
                .append("type", type)
                .append("tenantId", tenantId)
                .append("parameters", parameters)
                .append("retryCount", retryCount)
                .append("executionThreshold", executionThreshold)
                .append("executionCount", executionCount)
                .append("registrationDate", registrationDate)
                .append("abnormalExecutionDetected", abnormalExecutionDetected)
                .toString();
    }

}
