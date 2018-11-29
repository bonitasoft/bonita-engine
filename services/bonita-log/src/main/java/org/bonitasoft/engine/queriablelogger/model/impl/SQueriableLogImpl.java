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
package org.bonitasoft.engine.queriablelogger.model.impl;

import java.util.Calendar;
import java.util.Date;

import lombok.Data;
import org.bonitasoft.engine.queriablelogger.model.SQueriableLog;
import org.bonitasoft.engine.queriablelogger.model.SQueriableLogSeverity;

/**
 * @author Elias Ricken de Medeiros
 * @author Celine Souchet
 * @author Matthieu Chaffotte
 */
@Data
public class SQueriableLogImpl implements SQueriableLog {


    private long tenantId;
    private long id;
    private long timeStamp;
    private int year;
    private int month;
    private int dayOfYear;
    private int weekOfYear;
    private String userId;
    private final long threadNumber;
    private String clusterNode;
    private String productVersion;
    private SQueriableLogSeverity severity;
    private String actionType;
    private String actionScope;
    private int actionStatus;
    private String rawMessage;
    private String callerClassName;
    private String callerMethodName;
    private long numericIndex1;
    private long numericIndex2;
    private long numericIndex3;
    private long numericIndex4;
    private long numericIndex5;

    public SQueriableLogImpl() {
        numericIndex1 = -1;
        numericIndex2 = -1;
        numericIndex3 = -1;
        numericIndex4 = -1;
        numericIndex5 = -1;

        threadNumber = Thread.currentThread().getId();
        severity = null;
        actionStatus = -1;

        final Date date = new Date();
        updateDateElements(date);
    }

    @Override
    public long getNumericIndex(final int pos) {
        long result;
        switch (pos) {
            case 0:
                result = numericIndex1;
                break;
            case 1:
                result = numericIndex2;
                break;
            case 2:
                result = numericIndex3;
                break;
            case 3:
                result = numericIndex4;
                break;
            case 4:
                result = numericIndex5;
                break;
            default:
                throw new IllegalStateException();
        }
        return result;
    }

    private void updateDateElements(final Date date) {
        timeStamp = date.getTime();

        final Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);

        year = calendar.get(Calendar.YEAR);
        month = calendar.get(Calendar.MONTH) + 1; // January is 0

        weekOfYear = calendar.get(Calendar.WEEK_OF_YEAR);
        dayOfYear = calendar.get(Calendar.DAY_OF_YEAR);
    }

    public void setNumericIndex(final int pos, final long value) {
        switch (pos) {
            case 0:
                numericIndex1 = value;
                break;
            case 1:
                numericIndex2 = value;
                break;
            case 2:
                numericIndex3 = value;
                break;
            case 3:
                numericIndex4 = value;
                break;
            case 4:
                numericIndex5 = value;
                break;
            default:
                throw new IllegalStateException();
        }
    }

}
