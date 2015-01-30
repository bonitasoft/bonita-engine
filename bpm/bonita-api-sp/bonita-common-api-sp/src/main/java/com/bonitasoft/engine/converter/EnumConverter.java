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

/**
 * @author Elias Ricken de Medeiros
 * @deprecated for internal use only. Please, don't use this class
 */
@Deprecated
public class EnumConverter {

    public <SOURCE extends Enum<SOURCE>, TARGET extends Enum<TARGET>, VALUE> Map<TARGET, VALUE> convert(Map<SOURCE, VALUE> toConvert, Class<TARGET> targetEnumClass) {
        final Map<TARGET, VALUE> fields = new HashMap<TARGET, VALUE>(toConvert.size());
        for (final Map.Entry<SOURCE, VALUE> entry : toConvert.entrySet()) {
            fields.put(Enum.valueOf(targetEnumClass, entry.getKey().name()), entry.getValue());
        }
        return fields;
    }
}
