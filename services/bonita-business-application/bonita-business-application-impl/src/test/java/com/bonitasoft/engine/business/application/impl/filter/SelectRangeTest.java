/*******************************************************************************
 * Copyright (C) 2014 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 ******************************************************************************/

package com.bonitasoft.engine.business.application.impl.filter;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

public class SelectRangeTest {

    @Test
    public void getStartIndex_should_return_startIndex_used_in_constructor() throws Exception {
        //given
        SelectRange range = new SelectRange(5, 10);

        //then
        assertThat(range.getStartIndex()).isEqualTo(5);
    }

    @Test
    public void getMaxResults_should_return_maxResults_used_in_constructor() throws Exception {
        //given
        SelectRange range = new SelectRange(5, 10);

        //then
        assertThat(range.getMaxResults()).isEqualTo(10);
    }
}
