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
package org.bonitasoft.engine.queriablelogger.model.builder;

import org.bonitasoft.engine.queriablelogger.model.SQueriableLog;
import org.bonitasoft.engine.queriablelogger.model.SQueriableLogSeverity;

/**
 * @author Elias Ricken de Medeiros
 * @author Nicolas Chabanoles
 * @author Matthieu Chaffotte
 */
public interface SLogBuilder {

    SLogBuilder userId(String userId);

    SLogBuilder clusterNode(String clusterNode);

    SLogBuilder productVersion(String productVersion);

    SLogBuilder severity(SQueriableLogSeverity severity);

    SLogBuilder actionScope(String scope);

    SLogBuilder actionStatus(int status);

    SLogBuilder rawMessage(String rawMessage);

    SLogBuilder callerClassName(String callerClassName);

    SLogBuilder callerMethodName(String callerMethodName);

    SQueriableLog done();

}
