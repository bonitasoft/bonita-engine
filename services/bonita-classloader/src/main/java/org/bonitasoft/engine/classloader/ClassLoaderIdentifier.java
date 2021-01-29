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

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.bonitasoft.engine.dependency.model.ScopeType;

/**
 * @author Baptiste Mesta
 */
@Data
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ClassLoaderIdentifier implements Serializable {

    public static final ScopeType GLOBAL_TYPE = ScopeType.GLOBAL;
    public static final long GLOBAL_ID = -1;
    /**
     * The GLOBAL classloader is the unique one at platform level
     */
    public static final ClassLoaderIdentifier GLOBAL = identifier(GLOBAL_TYPE, GLOBAL_ID);
    /**
     * The APPLICATION classloader is the parent classloader of the GLOBAL classloader. It the one in which bonita is
     * bootstrapped
     */
    public static final ClassLoaderIdentifier APPLICATION = identifier(null, Long.MIN_VALUE);

    private ScopeType type;
    private long id;

    public static ClassLoaderIdentifier identifier(ScopeType scopeType, long id) {
        return new ClassLoaderIdentifier(scopeType, id);
    }

    @Override
    public String toString() {
        return type.name() + ':' + id;
    }
}
