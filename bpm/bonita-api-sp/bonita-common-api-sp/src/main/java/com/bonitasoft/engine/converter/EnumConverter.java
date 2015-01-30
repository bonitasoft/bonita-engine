/*******************************************************************************
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 ******************************************************************************/

package com.bonitasoft.engine.converter;

import java.util.HashMap;
import java.util.Map;

public class EnumConverter {

    public <S extends Enum<S>, D extends Enum<D>, V> Map<D, V> convert(Map<S, V> toConvert, Class<D> targetEnumClass) {
        final Map<D, V> fields = new HashMap<D, V>(toConvert.size());
        for (final Map.Entry<S, V> entry : toConvert.entrySet()) {
            fields.put(Enum.valueOf(targetEnumClass, entry.getKey().name()), entry.getValue());
        }
        return fields;
    }
}
