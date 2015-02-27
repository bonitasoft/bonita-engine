/*******************************************************************************
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.page.impl;

import static org.assertj.core.api.Assertions.assertThat;

import org.bonitasoft.engine.impl.PageImpl;
import org.junit.Before;
import org.junit.Test;

import com.bonitasoft.engine.page.Page;

public class PageConverterTest {

    private PageConverter converter;

    @Before
    public void setUp() throws Exception {
        converter = new PageConverter();
    }

    @Test
    public void convert_should_map_all_fields() throws Exception {
        //given
        final long now = System.currentTimeMillis();
        final PageImpl sourcePage = new PageImpl(1, "name", "display", true, "description", now, 2, now + 10, 3, "contentName");

        //when
        final Page convertedPage = converter.convert(sourcePage);

        //then
        assertThat(convertedPage.getId()).isEqualTo(1);
        assertThat(convertedPage.getName()).isEqualTo("name");
        assertThat(convertedPage.getDisplayName()).isEqualTo("display");
        assertThat(convertedPage.isProvided()).isTrue();
        assertThat(convertedPage.getDescription()).isEqualTo("description");
        assertThat(convertedPage.getInstallationDate().getTime()).isEqualTo(now);
        assertThat(convertedPage.getInstalledBy()).isEqualTo(2);
        assertThat(convertedPage.getLastModificationDate().getTime()).isEqualTo(now + 10);
        assertThat(convertedPage.getLastUpdatedBy()).isEqualTo(3);
        assertThat(convertedPage.getContentName()).isEqualTo("contentName");

    }
}
