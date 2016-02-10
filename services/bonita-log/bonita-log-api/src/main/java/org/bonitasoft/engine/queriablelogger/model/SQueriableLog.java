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
package org.bonitasoft.engine.queriablelogger.model;

import org.bonitasoft.engine.persistence.PersistentObject;

/**
 * @author Nicolas Chabanoles
 * @author Elias Ricken de Medeiros
 * @author Celine Souchet
 * @author Matthieu Chaffotte
 */
public interface SQueriableLog extends PersistentObject {

    int STATUS_FAIL = 0;

    int STATUS_OK = 1;

    long getTimeStamp();

    int getYear();

    int getMonth();

    int getDayOfYear();

    int getWeekOfYear();

    String getUserId();

    long getThreadNumber();

    String getClusterNode();

    String getProductVersion();

    SQueriableLogSeverity getSeverity();

    String getActionType();

    String getActionScope();

    int getActionStatus();

    String getRawMessage();

    String getCallerClassName();

    String getCallerMethodName();

    long getNumericIndex(final int pos);

}
