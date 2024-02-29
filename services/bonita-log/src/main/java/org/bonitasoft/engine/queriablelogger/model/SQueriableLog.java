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
package org.bonitasoft.engine.queriablelogger.model;

import java.util.Calendar;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bonitasoft.engine.persistence.PersistentObject;
import org.bonitasoft.engine.persistence.PersistentObjectId;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
@IdClass(PersistentObjectId.class)
@Table(name = "queriable_log")
public class SQueriableLog implements PersistentObject {

    public static final int STATUS_FAIL = 0;
    public static final int STATUS_OK = 1;
    public static final String TIME_STAMP = "timeStamp";
    public static final String YEAR = "year";
    public static final String MONTH = "month";
    public static final String DAY_OF_YEAR = "dayOfYear";
    public static final String WEEK_OF_YEAR = "weekOfYear";
    public static final String USERID = "userId";
    public static final String THREAD_NUMBER = "threadNumber";
    public static final String CLUSTER_NODE = "clusterNode";
    public static final String PRODUCT_VERSION = "productVersion";
    public static final String SEVERTITY = "severity";
    public static final String ACTION_TYPE = "actionType";
    public static final String ACTION_SCOPE = "actionScope";
    public static final String ACTION_STATUS = "actionStatus";
    public static final String RAW_MESSAGE = "rawMessage";
    public static final String CALLER_CLASS_NAME = "callerClassName";
    public static final String CALLER_METHOD_NAME = "callerMethodName";
    public static final String NUMERIC_INDEX1 = "numericIndex1";
    public static final String NUMERIC_INDEX2 = "numericIndex2";
    public static final String NUMERIC_INDEX3 = "numericIndex3";
    public static final String NUMERIC_INDEX4 = "numericIndex4";
    public static final String NUMERIC_INDEX5 = "numericIndex5";

    @Id
    private long tenantId;
    @Id
    private long id;
    @Column(name = "log_timestamp")
    private long timeStamp;
    @Column(name = "whatYear")
    private int year;
    @Column(name = "whatMonth")
    private int month;
    private int dayOfYear;
    private int weekOfYear;
    private String userId;
    @Builder.Default
    private long threadNumber = Thread.currentThread().getId();
    private String clusterNode;
    private String productVersion;
    @Enumerated(EnumType.STRING)
    private SQueriableLogSeverity severity;
    private String actionType;
    private String actionScope;
    @Builder.Default
    private int actionStatus = -1;
    @Column(name = "RAWMESSAGE")
    private String rawMessage;
    private String callerClassName;
    private String callerMethodName;
    @Builder.Default
    private long numericIndex1 = -1;
    @Builder.Default
    private long numericIndex2 = -1;
    @Builder.Default
    private long numericIndex3 = -1;
    @Builder.Default
    private long numericIndex4 = -1;
    @Builder.Default
    private long numericIndex5 = -1;

    public static class SQueriableLogBuilder {

        public SQueriableLogBuilder initializeNow() {
            final Date date = new Date();
            final Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);
            timeStamp(date.getTime());
            year(calendar.get(Calendar.YEAR));
            month(calendar.get(Calendar.MONTH) + 1);
            weekOfYear(calendar.get(Calendar.WEEK_OF_YEAR));
            dayOfYear(calendar.get(Calendar.DAY_OF_YEAR));
            return this;
        }
    }

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
