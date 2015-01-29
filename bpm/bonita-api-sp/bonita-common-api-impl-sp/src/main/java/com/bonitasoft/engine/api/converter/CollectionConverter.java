/*******************************************************************************
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 ******************************************************************************/
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
