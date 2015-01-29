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
package com.bonitasoft.engine.api.converter;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Emmanuel Duchastenier
 */
public class CollectionConverter<S, D> {

    private final Converter<S, D> converter;

    public CollectionConverter(final Converter<S, D> convertor) {
        this.converter = convertor;

    }

    public List<D> convert(final List<S> toConvert) {
        final ArrayList<D> converted = new ArrayList<D>(toConvert.size());
        for (final S source : toConvert) {
            converted.add(converter.convert(source));
        }
        return converted;
    }

}
