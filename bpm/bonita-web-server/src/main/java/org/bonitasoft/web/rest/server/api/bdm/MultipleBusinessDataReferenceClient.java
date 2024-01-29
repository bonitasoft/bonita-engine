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
package org.bonitasoft.web.rest.server.api.bdm;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author Baptiste Mesta
 */
public class MultipleBusinessDataReferenceClient extends BusinessDataReferenceClient {

    private final List<Long> storageIds;

    @JsonProperty("storageIds_string")
    private final List<String> storageIdsAsString;

    public MultipleBusinessDataReferenceClient(String name, String type, String link, List<Long> storageIds) {
        super(name, type, link);
        this.storageIds = storageIds;
        storageIdsAsString = new ArrayList<>();
        for (Long storageId : storageIds) {
            storageIdsAsString.add(storageId.toString());
        }
    }

    public List<Long> getStorageIds() {
        return storageIds;
    }

    public List<String> getStorageIdsAsString() {
        return storageIdsAsString;
    }

}
