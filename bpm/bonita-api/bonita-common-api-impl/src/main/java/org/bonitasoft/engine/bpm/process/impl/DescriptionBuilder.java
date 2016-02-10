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
package org.bonitasoft.engine.bpm.process.impl;

/**
 * Builder for all definition elements that have a description.
 *
 * @author Matthieu Chaffotte
 * @author Emmanuel Duchastenier
 */
public interface DescriptionBuilder {

    /**
     * Adds a description to current element.
     *
     * @param description the description to set on this element.
     * @return this <code>DescriptionBuilder</code>, to allow to chain the calls.
     */
    DescriptionBuilder addDescription(final String description);

}
