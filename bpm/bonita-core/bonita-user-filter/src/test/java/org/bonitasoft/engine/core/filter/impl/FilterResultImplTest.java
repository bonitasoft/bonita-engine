/*
 * Copyright (C) 2017 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 */

package org.bonitasoft.engine.core.filter.impl;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

/**
 * @author Danila Mazour
 */

public class FilterResultImplTest {

    @Test
    public void filterResult_should_not_return_duplicates() {
        //given
        List<Long> listWithDuplicates = Arrays.asList(15L, 15L, 15L, 28L, 32L, 28L, 39L);
        FilterResultImpl filterResult = new FilterResultImpl(listWithDuplicates, true);

        //when
        List<Long> result = filterResult.getResult();

        //then
        assertThat(result).containsExactly(15L, 28L, 32L, 39L);
    }

}
