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
package org.bonitasoft.engine.classloader;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.bonitasoft.engine.dependency.model.ScopeType;

/**
 * @author Baptiste Mesta
 */
@Data
@AllArgsConstructor
public class ClassLoaderIdentifier implements Serializable {

    public static final String GLOBAL_TYPE = "GLOBAL";
    public static final long GLOBAL_ID = -1;
    public static final ClassLoaderIdentifier GLOBAL = new ClassLoaderIdentifier(GLOBAL_TYPE, GLOBAL_ID);

    private String type;
    private long id;

    public static ClassLoaderIdentifier identifier(ScopeType scopeType, long id) {
        return new ClassLoaderIdentifier(scopeType.name(), id);
    }

    public ScopeType getScopeType() {
        return ScopeType.valueOf(type);
    }

    @Override
    public String toString() {
        return type + ':' + id;
    }
}
