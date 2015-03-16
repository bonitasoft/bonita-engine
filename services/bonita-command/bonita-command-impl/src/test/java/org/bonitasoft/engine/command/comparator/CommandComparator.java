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
package org.bonitasoft.engine.command.comparator;

import java.util.Comparator;

import org.bonitasoft.engine.command.model.SCommand;

/**
 * @author Elias Ricken de Medeiros
 */
public class CommandComparator implements Comparator<SCommand> {

    @Override
    public int compare(final SCommand o1, final SCommand o2) {
        if (o1.getName().equals(o2.getName()) && o1.getDescription().equals(o2.getDescription()) && o1.getImplementation().equals(o2.getImplementation())) {
            return 0;
        }
        return 1;
    }
}
