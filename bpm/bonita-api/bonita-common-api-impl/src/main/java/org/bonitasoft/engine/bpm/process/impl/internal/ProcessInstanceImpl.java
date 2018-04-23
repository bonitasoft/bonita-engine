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
package org.bonitasoft.engine.bpm.process.impl.internal;

import java.util.Date;

import org.bonitasoft.engine.bpm.internal.NamedElementImpl;
import org.bonitasoft.engine.bpm.process.ProcessInstance;

/**
 * @author Baptiste Mesta
 * @author Matthieu Chaffotte
 * @author Celine Souchet
 */
public class ProcessInstanceImpl extends NamedElementImpl implements ProcessInstance {

    private static final long serialVersionUID = 7745012384930764581L;

    private String state;

    private Date startDate;

    private long startedBy;

    private long startedBySubstitute;

    private Date endDate;

    private Date lastUpdate;

    private long processDefinitionId;

    private String description;

    private long rootProcessInstanceId;

    private long callerId;

    private String stringIndex1;

    private String stringIndex2;

    private String stringIndex3;

    private String stringIndex4;

    private String stringIndex5;

    private String stringIndexLabel1;

    private String stringIndexLabel2;

    private String stringIndexLabel3;

    private String stringIndexLabel4;

    private String stringIndexLabel5;

    public ProcessInstanceImpl(final String name) {
        super(name);
    }

    @Override
    public String getState() {
        return state;
    }

    public void setState(final String state) {
        this.state = state;
    }

    @Override
    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(final Date startDate) {
        this.startDate = startDate;
    }

    @Override
    public long getStartedBy() {
        return startedBy;
    }

    public void setStartedBy(final long startedBy) {
        this.startedBy = startedBy;
    }

    @Override
    public long getStartedBySubstitute() {
        return startedBySubstitute;
    }

    public void setStartedBySubstitute(final long startedBySubstitute) {
        this.startedBySubstitute = startedBySubstitute;
    }

    @Deprecated
    @Override
    public long getStartedByDelegate() {
        return getStartedBySubstitute();
    }

    @Deprecated
    public void setStartedByDelegate(long startedByDelegate) {
        setStartedBySubstitute(startedByDelegate);
    }

    @Override
    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(final Date endDate) {
        this.endDate = endDate;
    }

    @Override
    public Date getLastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdate(final Date lastUpdate) {
        this.lastUpdate = lastUpdate;
    }

    @Override
    public long getProcessDefinitionId() {
        return processDefinitionId;
    }

    public void setProcessDefinitionId(final long processDefinitionId) {
        this.processDefinitionId = processDefinitionId;
    }

    @Override
    public String getDescription() {
        return description;
    }

    public void setDescription(final String description) {
        this.description = description;
    }

    @Override
    public long getRootProcessInstanceId() {
        return rootProcessInstanceId;
    }

    public void setRootProcessInstanceId(final long rootProcessInstanceId) {
        this.rootProcessInstanceId = rootProcessInstanceId;
    }

    @Override
    public long getCallerId() {
        return callerId;
    }

    public void setCallerId(final long callerId) {
        this.callerId = callerId;
    }

    @Override
    public String getStringIndex1() {
        return stringIndex1;
    }

    public void setStringIndex1(final String stringIndex1) {
        this.stringIndex1 = stringIndex1;
    }

    @Override
    public String getStringIndex2() {
        return stringIndex2;
    }

    public void setStringIndex2(final String stringIndex2) {
        this.stringIndex2 = stringIndex2;
    }

    @Override
    public String getStringIndex3() {
        return stringIndex3;
    }

    public void setStringIndex3(final String stringIndex3) {
        this.stringIndex3 = stringIndex3;
    }

    @Override
    public String getStringIndex4() {
        return stringIndex4;
    }

    public void setStringIndex4(final String stringIndex4) {
        this.stringIndex4 = stringIndex4;
    }

    @Override
    public String getStringIndex5() {
        return stringIndex5;
    }

    public void setStringIndex5(final String stringIndex5) {
        this.stringIndex5 = stringIndex5;
    }

    public String getStringIndexLabel1() {
        return stringIndexLabel1;
    }

    public void setStringIndexLabel1(final String stringIndexLabel1) {
        this.stringIndexLabel1 = stringIndexLabel1;
    }

    public String getStringIndexLabel2() {
        return stringIndexLabel2;
    }

    public void setStringIndexLabel2(final String stringIndexLabel2) {
        this.stringIndexLabel2 = stringIndexLabel2;
    }

    public String getStringIndexLabel3() {
        return stringIndexLabel3;
    }

    public void setStringIndexLabel3(final String stringIndexLabel3) {
        this.stringIndexLabel3 = stringIndexLabel3;
    }

    public String getStringIndexLabel4() {
        return stringIndexLabel4;
    }

    public void setStringIndexLabel4(final String stringIndexLabel4) {
        this.stringIndexLabel4 = stringIndexLabel4;
    }

    public String getStringIndexLabel5() {
        return stringIndexLabel5;
    }

    public void setStringIndexLabel5(final String stringIndexLabel5) {
        this.stringIndexLabel5 = stringIndexLabel5;
    }

    public void setStringIndexLabel(final int index, final String label) {
        switch (index) {
            case 1:
                stringIndexLabel1 = label;
                break;
            case 2:
                stringIndexLabel2 = label;
                break;
            case 3:
                stringIndexLabel3 = label;
                break;
            case 4:
                stringIndexLabel4 = label;
                break;
            case 5:
                stringIndexLabel5 = label;
                break;
            default:
                throw new IndexOutOfBoundsException("string index label must be between 1 and 5 (included)");
        }
    }

    @Override
    public String getStringIndexLabel(final int index) {
        switch (index) {
            case 1:
                return stringIndexLabel1;
            case 2:
                return stringIndexLabel2;
            case 3:
                return stringIndexLabel3;
            case 4:
                return stringIndexLabel4;
            case 5:
                return stringIndexLabel5;
            default:
                throw new IndexOutOfBoundsException("string index label must be between 1 and 5 (included)");
        }
    }

}
