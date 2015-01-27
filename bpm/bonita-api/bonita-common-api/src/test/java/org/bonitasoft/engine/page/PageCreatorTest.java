package org.bonitasoft.engine.page;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.Serializable;
import java.util.Map;

import org.bonitasoft.engine.page.PageCreator.PageField;
import org.junit.Test;

public class PageCreatorTest {

    private static final String CONTENT_ZIP = "content.zip";

    private static final String DISPLAY_NAME = "display name";

    private static final String NAME = "page name";

    private static final String DESCRIPTION = "page description";

    @Test
    public void pageCreatorWithName() {
        // given
        final PageCreator pageCreator = new PageCreator(NAME, CONTENT_ZIP);

        // wwhen
        final Map<PageField, Serializable> fields = pageCreator.getFields();

        // then
        assertThat(fields).as("field size should contains name entry").hasSize(2).containsKey(PageField.NAME).doesNotContainKey(PageField.DESCRIPTION);
        assertThat(fields.get(PageField.NAME)).as("name file should be").isEqualTo(NAME);
        assertThat(fields.get(PageField.CONTENT_NAME)).as("content name should be").isEqualTo(CONTENT_ZIP);

    }

    @Test
    public void pageCreatorWithDescrition() {
        // given
        final PageCreator pageCreator = new PageCreator(NAME, CONTENT_ZIP);
        pageCreator.setDescription(DESCRIPTION);

        // when
        final Map<PageField, Serializable> fields = pageCreator.getFields();

        // then
        assertThat(fields).as("field size should contains name entry").hasSize(3).containsKey(PageField.NAME).containsKey(PageField.DESCRIPTION);
        assertThat(fields.get(PageField.NAME)).as("name file should be " + NAME).isEqualTo(NAME);
        assertThat(fields.get(PageField.CONTENT_NAME)).as("name file should be").isEqualTo(CONTENT_ZIP);
        assertThat(fields.get(PageField.DESCRIPTION)).as("description entry should be " + DESCRIPTION).isEqualTo(DESCRIPTION);

    }

    @Test
    public void pageCreatorWithDisplayName() {
        // given
        final PageCreator pageCreator = new PageCreator(NAME, CONTENT_ZIP);
        pageCreator.setDisplayName(DISPLAY_NAME);

        // when
        final Map<PageField, Serializable> fields = pageCreator.getFields();

        // then
        assertThat(fields).as("field size should contains name entry").hasSize(3).containsKey(PageField.NAME).containsKey(PageField.DISPLAY_NAME);
        assertThat(fields.get(PageField.NAME)).as("name file should be " + NAME).isEqualTo(NAME);
        assertThat(fields.get(PageField.CONTENT_NAME)).as("name file should be").isEqualTo(CONTENT_ZIP);
        assertThat(fields.get(PageField.DISPLAY_NAME)).as("display name entry should be " + DISPLAY_NAME).isEqualTo(DISPLAY_NAME);

    }

}
