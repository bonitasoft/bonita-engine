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
package org.bonitasoft.engine.core.form;

import org.bonitasoft.engine.page.SPageMapping;
import org.bonitasoft.engine.persistence.PersistentObject;

/**
 * @author Baptiste Mesta
 */
public interface SFormMapping extends PersistentObject {

    String TARGET_INTERNAL = "INTERNAL";
    String TARGET_URL = "URL";
    String TARGET_LEGACY = "LEGACY";
    String TARGET_UNDEFINED = "UNDEFINED";

    int TYPE_PROCESS_START = 1;
    int TYPE_PROCESS_OVERVIEW = 2;
    int TYPE_TASK = 3;

    long getTenantId();

    long getProcessDefinitionId();

    String getTask();

    SPageMapping getPageMapping();

    Integer getType();

    long getLastUpdateDate();

    long getLastUpdatedBy();

    String getTarget();
}
