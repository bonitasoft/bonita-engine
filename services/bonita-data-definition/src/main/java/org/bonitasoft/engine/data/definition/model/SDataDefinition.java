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
package org.bonitasoft.engine.data.definition.model;

import java.io.Serializable;

import org.bonitasoft.engine.expression.model.SExpression;

/**
 * @author Zhao Na
 * @author Matthieu Chaffotte
 */
public interface SDataDefinition extends Serializable {

    String getName();

    String getDescription();

    String getClassName();

    /**
     * BPA-833: primitive <code>boolean</code>, not the wrapper. The JavaBeans specification sanctions the
     * <code>is</code> accessor only for a primitive, and Groovy 4 enforces that - a wrapper-<code>Boolean</code>
     * <code>isX()</code> is not a property getter, so <code>definition.transientData</code> would fail on any
     * subclass receiver, and <code>SDataDefinitionImpl</code> is subclassed. The backing field has always been a
     * primitive and no implementation can return <code>null</code>, so the wrapper was gratuitous.
     * <p>
     * <strong>Narrowing the return type is a binary break, taken deliberately</strong> - no bridge method is
     * generated, so 11.x-compiled callers get a <code>NoSuchMethodError</code>, and this interface ships in the
     * <code>bonita-server</code> fat jar where extensions can reach it. Accepted because this is a server-internal
     * SPI and 12.0 is the major-version window; the public <code>DataInstance</code> and
     * <code>MaintenanceDetails</code> took the source-compatible route instead. The full reasoning, and why the two
     * routes differ, is in <code>doc/groovy_3_to_4-specific-study/</code>; the migration note is on BPA-729.
     */
    boolean isTransientData();

    SExpression getDefaultValueExpression();
}
