/**
 * Copyright (C) 2014 BonitaSoft S.A.
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
package com.bonitasoft.engine.business.application;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;


/**
 * @author Elias Ricken de Medeiros
 *
 */
public class ApplicationCreator implements Serializable {

    private static final long serialVersionUID = -916041825489100271L;

    public enum ApplicationField {
        NAME, VERSION, URL, DESCRIPTION;
    }

    private final Map<ApplicationField, Serializable> fields;

    public ApplicationCreator(final String name, final String version, final String url) {
        fields = new HashMap<ApplicationField, Serializable>(2);
        fields.put(ApplicationField.NAME, name);
        fields.put(ApplicationField.VERSION, version);
        fields.put(ApplicationField.URL, url);
    }

    public String getName() {
        return fields.get(ApplicationField.NAME).toString();
    }

    public ApplicationCreator setDescription(final String description) {
        fields.put(ApplicationField.DESCRIPTION, description);
        return this;
    }

    public Map<ApplicationField, Serializable> getFields() {
        return fields;
    }

}
