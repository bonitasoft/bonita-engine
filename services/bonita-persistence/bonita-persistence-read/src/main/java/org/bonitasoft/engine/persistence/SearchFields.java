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
package org.bonitasoft.engine.persistence;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Matthieu Chaffotte
 */
public class SearchFields implements Serializable {

    private static final long serialVersionUID = -9157314460334148998L;

    private final List<String> terms;

    private final Map<Class<? extends PersistentObject>, Set<String>> fields;

    public SearchFields(final List<String> terms, final Map<Class<? extends PersistentObject>, Set<String>> fields) {
        super();
        this.terms = terms;
        this.fields = fields;
    }

    public List<String> getTerms() {
        return terms;
    }

    public Map<Class<? extends PersistentObject>, Set<String>> getFields() {
        return fields;
    }

}
