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
package org.bonitasoft.engine.core.process.instance.model;

/***
 * Represents flownode state counters for a given process instance. Only serves query purposes (like a view), not persisted.
 * 
 * @author Emmanuel Duchastenier
 */
public class SFlowNodeInstanceStateCounter {

    private String flownodeName;
    private String stateName;
    private Long numberOf;

    public SFlowNodeInstanceStateCounter(String flownodeName, String stateName, Long numberOf) {
        this.flownodeName = flownodeName;
        this.stateName = stateName;
        this.numberOf = numberOf;
    }

    public String getFlownodeName() {
        return flownodeName;
    }

    public String getStateName() {
        return stateName;
    }

    public Long getNumberOf() {
        return numberOf;
    }
}
