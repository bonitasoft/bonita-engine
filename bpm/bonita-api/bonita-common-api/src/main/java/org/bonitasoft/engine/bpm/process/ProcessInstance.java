/**
 * Copyright (C) 2012-2014 BonitaSoft S.A.
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
package org.bonitasoft.engine.bpm.process;

import java.util.Date;

import org.bonitasoft.engine.bpm.BaseElement;
import org.bonitasoft.engine.bpm.NamedElement;

/**
 * @author Baptiste Mesta
 * @author Matthieu Chaffotte
 * @author Celine Souchet
 */
public interface ProcessInstance extends NamedElement, BaseElement {

    String getState();

    Date getStartDate();

    /**
     * @return The identifier of the user who started the process
     * @since 6.0.1
     */
    long getStartedBy();

    /**
     * @return The identifier of the substitute user (as Process manager or Administrator) who started the process.
     * @since 6.3.0
     */
    long getStartedBySubstitute();

    /**
     * @return The identifier of the substitute user (as Process manager or Administrator) who started the process.
     * @since 6.0.1
     * @deprecated since 6.3.0, use {@link ProcessInstance#getStartedBySubstitute()}
     */
    @Deprecated
    long getStartedByDelegate();

    Date getEndDate();

    Date getLastUpdate();

    long getProcessDefinitionId();

    String getDescription();

    long getRootProcessInstanceId();

    /**
     * 
     * @return id of the call activity instance
     */
    long getCallerId();

    String getStringIndex1();

    String getStringIndex2();

    String getStringIndex3();

    String getStringIndex4();

    String getStringIndex5();

    /**
     * get the label of the string index
     * 
     * @param index
     * @return label of the string index
     */
    String getStringIndexLabel(int index);

}
