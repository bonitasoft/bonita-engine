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
package com.bonitasoft.engine.log;

import java.io.Serializable;
import java.util.Date;

/**
 * @author Lu Kai
 * @author Bole Zhang
 */
public interface Log extends Serializable {

    public long getLogId();

    public String getMessage();

    public SeverityLevel getSeverityLevel();

    public String getCreatedBy();

    public Date getCreationDate();

    public String getActionType();

    public String getActionScope();

    public SeverityLevel getSeverity();

    public String getCallerClassName();

    public String getCallerMethodName();

}
