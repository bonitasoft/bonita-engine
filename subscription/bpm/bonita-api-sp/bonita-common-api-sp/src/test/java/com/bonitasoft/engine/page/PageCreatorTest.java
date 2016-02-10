/*******************************************************************************
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.page;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;

import java.io.Serializable;
import java.util.Map;

import com.bonitasoft.engine.page.PageCreator.PageField;
import org.bonitasoft.engine.page.ContentType;
import org.junit.Test;

public class PageCreatorTest {

    private static final String CONTENT_ZIP = "content.zip";

    private static final String DISPLAY_NAME = "display name";

    private static final String NAME = "page name";

    private static final String DESCRIPTION = "page description";
    public static final String CONTENT_TYPE = "form";
    public static final long PROCESS_DEFINITION_ID = 1L;

    @Test
    public void pageCreatorWithName() {
        // given
        final PageCreator pageCreator = new PageCreator(NAME, CONTENT_ZIP);

        // wwhen
        final Map<PageField, Serializable> fields = pageCreator.getFields();

        // then
        assertThat(fields).as("field size should contains name entry").containsOnly(
                entry(PageField.NAME, NAME),
                entry(PageField.CONTENT_NAME, CONTENT_ZIP),
                entry(PageField.CONTENT_TYPE,  ContentType.PAGE));
    }

    @Test
    public void pageCreatorWithDescrition() {
        // given
        final PageCreator pageCreator = new PageCreator(NAME, CONTENT_ZIP);
        pageCreator.setDescription(DESCRIPTION);

        // when
        final Map<PageField, Serializable> fields = pageCreator.getFields();

        // then
        assertThat(fields).as("field size should contains name entry").containsOnly(
                entry(PageField.NAME, NAME),
                entry(PageField.CONTENT_NAME, CONTENT_ZIP),
                entry(PageField.DESCRIPTION, DESCRIPTION),
                entry(PageField.CONTENT_TYPE,  ContentType.PAGE));

    }

    @Test
    public void pageCreatorWithDisplayName() {
        // given
        final PageCreator pageCreator = new PageCreator(NAME, CONTENT_ZIP);
        pageCreator.setDisplayName(DISPLAY_NAME);

        // when
        final Map<PageField, Serializable> fields = pageCreator.getFields();

        // then
        assertThat(fields).as("field size should contains name entry").containsOnly(
                entry(PageField.NAME, NAME),
                entry(PageField.CONTENT_NAME, CONTENT_ZIP),
                entry(PageField.DISPLAY_NAME, DISPLAY_NAME),
                entry(PageField.CONTENT_TYPE, ContentType.PAGE));
    }

    @Test
    public void pageCreatorWithContentTypeAndProcessDefinitionID() {
        // given
        final PageCreator pageCreator = new PageCreator(NAME, CONTENT_ZIP, ContentType.FORM, PROCESS_DEFINITION_ID);
        pageCreator.setDisplayName(DISPLAY_NAME);

        // when
        final Map<PageField, Serializable> fields = pageCreator.getFields();

        // then
        assertThat(fields).as("field size should contains name entry").containsOnly(
                entry(PageField.NAME, NAME),
                entry(PageField.CONTENT_NAME, CONTENT_ZIP),
                entry(PageField.DISPLAY_NAME, DISPLAY_NAME),
                entry(PageField.CONTENT_TYPE, ContentType.FORM),
                entry(PageField.PROCESS_DEFINITION_ID, PROCESS_DEFINITION_ID));

    }

}
