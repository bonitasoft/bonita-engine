/**
 * Copyright (C) 2017 Bonitasoft S.A.
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

package org.bonitasoft.engine.business.data.impl.jackson;

import org.bonitasoft.engine.business.data.impl.jackson.writer.LinkPropertyWriter;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonAppend;

@JsonAppend(props = {
        @JsonAppend.Prop(value = LinkPropertyWriter.class, name = EntityMixin.PROPERTY_LINK, include = JsonInclude.Include.NON_EMPTY)
})
public abstract class EntityMixin {

    public static final String PROPERTY_LINK = "links";

}
