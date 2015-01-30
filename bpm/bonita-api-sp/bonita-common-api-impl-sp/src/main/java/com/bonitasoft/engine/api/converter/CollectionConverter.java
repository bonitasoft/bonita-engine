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
public class CollectionConverter {

    public <SOURCE, DESTINATION> List<DESTINATION> convert(final List<SOURCE> toConvert, final Converter<SOURCE, DESTINATION> converter) {
        final ArrayList<DESTINATION> converted = new ArrayList<DESTINATION>(toConvert.size());
        for (final SOURCE source : toConvert) {
            converted.add(converter.convert(source));
        }
        return converted;
    }

}
