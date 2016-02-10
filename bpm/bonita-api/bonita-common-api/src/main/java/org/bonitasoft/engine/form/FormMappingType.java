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
package org.bonitasoft.engine.form;

/**
 * @author Baptiste Mesta
 */
public enum FormMappingType {

    /**
     * form used as process start form
     */
    PROCESS_START(1),

    /**
     * form used as process overview form
     */
    PROCESS_OVERVIEW(2),

    /**
     * form of a task
     */
    TASK(3);

    private Integer id;

    public Integer getId() {
        return id;
    }

    FormMappingType(Integer id) {
        this.id = id;
    }

    public static FormMappingType getTypeFromId(Integer type) {
        if (type != null) {
            for (FormMappingType formMappingType : values()) {
                if (formMappingType.getId() == type.intValue()) {
                    return formMappingType;
                }
            }
        }
        return null;
    }
}
