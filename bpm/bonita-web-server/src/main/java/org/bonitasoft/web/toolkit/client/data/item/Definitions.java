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

import java.util.HashMap;
import java.util.Map;

import org.bonitasoft.web.toolkit.client.ItemDefinitionFactory;

/**
 * @author Julien Mege
 */
public class Definitions {

    private final Map<String, ItemDefinition<?>> itemDefinitions = new HashMap<>();

    private static final Definitions INSTANCE = new Definitions();

    /**
     * Get the ViewController instance.
     *
     * @return the unique instance of the ViewController.
     */
    public static Definitions getInstance() {
        return INSTANCE;
    }

    public static ItemDefinition<?> get(final String token) {
        return getInstance().getDefinition(token);
    }

    public final ItemDefinition<?> getDefinition(final String token) {
        if (itemDefinitions.containsKey(token)) {
            return itemDefinitions.get(token);
        } else if (DummyItemDefinition.TOKEN.equals(token)) {
            return new DummyItemDefinition();
        } else {
            final ItemDefinition<?> itemDefinition = ItemDefinitionFactory.getDefaultFactory()
                    .defineItemDefinitions(token);
            if (itemDefinition != null) {
                itemDefinitions.put(token, itemDefinition);
                return itemDefinition;
            }
            // TODO Throw exception
            return null;
        }
    }

}
