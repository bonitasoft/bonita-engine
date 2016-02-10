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
package org.bonitasoft.engine.api;

/**
 * Contains all methods that handle processes.
 * Using this API you can handle:
 * <ul>
 * <li>{@link ProcessRuntimeAPI Execution of processes}: start process, retrieve tasks, execute tasks, retrieve data...</li>
 * <li> {@link ProcessManagementAPI Management of processes}: Deploy/Undeploy processes, enable/disable process...</li>
 * <li> {@link DocumentAPI Documents}: create, list, retrieve documents</li>
 * </ul>
 * 
 * @see ProcessRuntimeAPI
 * @see ProcessManagementAPI
 * @see DocumentAPI
 * @author Baptiste Mesta
 */
public interface ProcessAPI extends ProcessManagementAPI, ProcessRuntimeAPI, DocumentAPI {

}
