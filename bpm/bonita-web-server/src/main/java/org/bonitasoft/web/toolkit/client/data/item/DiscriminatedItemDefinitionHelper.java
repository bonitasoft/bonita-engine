/**
 * Copyright (C) 2022 Bonitasoft S.A.
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
package org.bonitasoft.web.toolkit.client.data.item;

import java.util.Map;
import java.util.function.Supplier;

import org.bonitasoft.web.rest.model.application.AbstractApplicationDefinition;
import org.bonitasoft.web.toolkit.client.common.TreeIndexed;

/**
 * This helps Items definitions which needs an attribute value to determine their concrete implementation.
 *
 * @see ItemDefinition#getDiscriminatedHelper()
 * @see AbstractApplicationDefinition#getDiscriminatedHelper() for an example
 * @param <E> the {@link IItem} which is defined, same as in {@link ItemDefinition}
 */
public interface DiscriminatedItemDefinitionHelper<E extends IItem> {

    /**
     * Find the appropriate creator with attributes to discriminate
     *
     * @param attributes the attributes for creation, one of which should be used to discriminate
     * @return the item creator
     */
    public Supplier<? extends E> findItemCreator(final Map<String, String> attributes);

    /**
     * Find the appropriate creator with properties tree to discriminate
     *
     * @param tree the tree of properties, one of which should be used to discriminate
     * @return the item creator
     */
    /*
     * We could delegate to the attributes method with a default implementation,
     * but it wouldn't be as effective as getting the direct value.
     */
    public Supplier<? extends E> findItemCreator(final TreeIndexed<String> tree);

}
