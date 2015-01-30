/*******************************************************************************
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 ******************************************************************************/

package com.bonitasoft.engine.converter;

import static org.assertj.core.api.Assertions.*;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

public class EnumConverterTest {

    @Test
    public void convert_should_return_a_map_with_equivalent_enum_in_target_type() throws Exception {
        //given
        HashMap<SourceEnum, String> toConvert = new HashMap<SourceEnum, String>();
        toConvert.put(SourceEnum.FIRST, "A");
        toConvert.put(SourceEnum.SECOND, "B");

        //when
        Map<TargetEnum, String> converted = new EnumConverter().convert(toConvert, TargetEnum.class);

        //then
        assertThat(converted).isNotNull();
        assertThat(converted).hasSize(2);
        assertThat(converted.get(TargetEnum.FIRST)).isEqualTo("A");
        assertThat(converted.get(TargetEnum.SECOND)).isEqualTo("B");

    }
}