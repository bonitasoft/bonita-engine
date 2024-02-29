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
package org.bonitasoft.web.toolkit.client.data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bonitasoft.web.toolkit.client.common.exception.api.APIException;
import org.bonitasoft.web.toolkit.client.common.exception.api.APIIncorrectIdException;
import org.bonitasoft.web.toolkit.client.common.exception.api.APIItemIdMalformedException;
import org.bonitasoft.web.toolkit.client.common.json.JSonSerializer;
import org.bonitasoft.web.toolkit.client.common.json.JsonSerializable;
import org.bonitasoft.web.toolkit.client.data.item.ItemDefinition;
import org.bonitasoft.web.toolkit.client.ui.utils.ListUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Julien Mege
 */
public class APIID implements JsonSerializable {

    private static final String SEPARATOR = "/";

    private final List<String> ids = new ArrayList<>();

    private ItemDefinition<?> itemDefinition = null;

    private static final Logger LOGGER = LoggerFactory.getLogger(APIID.class.getName());

    // //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // CONSTRUCTORS
    // //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private APIID(final String... id) {
        this(Arrays.asList(id));
    }

    private APIID(final Long... id) {
        for (final Long i : id) {
            this.ids.add(i != null ? String.valueOf(i) : null);
        }
    }

    private APIID(final List<String> ids) {
        // if the id passed is a serialized APIID
        if (ids.size() == 1 && ids.get(0).contains("/")) {
            this.ids.addAll(Arrays.asList(ids.get(0).split("/")));
        } else {
            this.ids.addAll(ids);
        }
    }

    public void setItemDefinition(final ItemDefinition<?> definition) {
        this.itemDefinition = definition;

        final int size = this.itemDefinition.getPrimaryKeys().size();

        if (this.ids.size() < size) {
            if (size == 0) {
                throw new APIException(this.itemDefinition.getClass().getName() + " is missing a valid primaryKey");
            }

            if (size == 1) {
                throw new APIException(
                        "Wrong APIID format for  [" + this.itemDefinition.getClass().getName() + "]." +
                                " This APIID must be a single id.");
            }

            throw new APIException(
                    "Wrong APIID format for  [" + this.itemDefinition.getClass().getName() + "]." +
                            " This APIID must be compound of [" +
                            ListUtils.join(this.itemDefinition.getPrimaryKeys(), ",") +
                            "] in this exact order.");
        }
    }

    // //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // STATIC CONSTRUCTORS
    // //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public static APIID makeAPIID(final String... id) {
        if (id == null) {
            return null;
        }
        return makeAPIID(Arrays.asList(id));
    }

    public static APIID makeAPIID(final Long... ids) {
        if (ids == null || ids.length == 0) {
            return null;
        }

        // If at least one id is not null
        for (final Long id : ids) {
            if (id != null && id > 0L) {
                return new APIID(ids);
            }
        }

        return null;
    }

    public static APIID makeAPIID(final List<String> ids) {
        if (ids == null || ids.size() == 0) {
            return null;
        }

        // If at least one id is not null
        for (final String id : ids) {
            if (id != null && !id.isEmpty()) {
                return new APIID(ids);
            }
        }

        return null;
    }

    // //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // GETTERS
    // //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public List<String> getIds() {
        return this.ids;
    }

    @Override
    public String toString() {
        String resourceId = "";
        if (this.ids != null && this.ids.size() > 0) {
            for (final String id : this.ids) {
                if (!"".equals(resourceId)) {
                    resourceId = resourceId + SEPARATOR;
                }
                resourceId = resourceId + id;
            }
        }
        return resourceId;
    }

    public boolean isValidLongID() {
        if (this.ids.size() == 1) {
            try {
                final long lid = Long.parseLong(this.ids.get(0));
                if (lid > 0L) {
                    return true;
                }
            } catch (final NumberFormatException e) {
                LOGGER.debug(this.ids.get(0) + " is not a valid long ID. ID must be non-zero positive number.");
            }
        } else {
            LOGGER.debug("ID is not a valid long ID. ID must not be multiple.");
        }
        return false;
    }

    public Long toLong() {
        if (this.ids.size() > 1) {
            throw new IllegalArgumentException("Can't convert compound ID to long");
        }

        try {
            final long lid = Long.parseLong(this.ids.get(0));
            if (lid > 0L) {
                return lid;
            } else {
                //zero or negative ids are not allowed
                String errorMessage = lid + " is not a valid long ID. ID must be non-zero positive.";
                LOGGER.debug(errorMessage);
                throw new APIIncorrectIdException(errorMessage);
            }
        } catch (final NumberFormatException e) {
            throw new APIItemIdMalformedException("APIID", "Can't convert non numeric ID to long");
        }

    }

    /*
     * Retrieve a part of the id with his index.
     * @return this method return a part of the id as a String.
     */
    public final String getPart(final int partIndex) {
        return this.ids.get(partIndex);
    }

    public final Long getPartAsLong(final int partIndex) {
        return Long.parseLong(getPart(partIndex));
    }

    public String getPart(final String attributeName) {
        final int index = this.itemDefinition.getPrimaryKeys().indexOf(attributeName);

        if (index == -1) {
            throw new APIException(attributeName +
                    " is an invalid APIID index. " +
                    "This APIID must be made of " +
                    ListUtils.join(this.itemDefinition.getPrimaryKeys(), ", ") +
                    " in this exact order.");
        }

        return this.ids.get(index);
    }

    public Long getPartAsLong(final String attributeName) {
        String part = getPart(attributeName);
        return part == null ? null : Long.valueOf(part);
    }

    public APIID getPartAsAPIID(final String attributeName) {
        return APIID.makeAPIID(getPart(attributeName));
    }

    public static List<Long> toLongList(final List<APIID> ids) {
        final List<Long> results = new ArrayList<>();
        for (final APIID id : ids) {
            results.add(id.toLong());
        }
        return results;
    }

    // //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // EQUALS
    // //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public int hashCode() {
        return toString().hashCode();
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == null) {
            return false;
        }
        return toString().equals(obj.toString());
    }

    // //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // JSON
    // //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public String toJson() {
        return JSonSerializer.serialize(this.ids);
    }

}
