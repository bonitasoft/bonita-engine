package com.bonitasoft.engine.page;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.Serializable;
import java.util.Map;

import org.junit.Test;

import com.bonitasoft.engine.page.PageCreator.PageField;

public class PageCreatorTest {

    private static final String DISPLAY_NAME = "display name";

    private static final String NAME = "page name";

    private static final String DESCRIPTION = "page description";

    @Test
    public void pageCreatorWithName() throws Exception {
        // given
        PageCreator pageCreator = new PageCreator(NAME, "content.zip");

        // wwhen
        Map<PageField, Serializable> fields = pageCreator.getFields();

        // then
        assertThat(fields).as("field size should contains name entry").hasSize(1).containsKey(PageField.NAME).doesNotContainKey(PageField.DESCRIPTION);
        assertThat(fields.get(PageField.NAME)).as("name file shouls be").isEqualTo(NAME);

    }

    @Test
    public void pageCreatorWithDescrition() throws Exception {
        // given
        PageCreator pageCreator = new PageCreator(NAME, "content.zip");
        pageCreator.setDescription(DESCRIPTION);

        // when
        Map<PageField, Serializable> fields = pageCreator.getFields();

        // then
        assertThat(fields).as("field size should contains name entry").hasSize(2).containsKey(PageField.NAME).containsKey(PageField.DESCRIPTION);
        assertThat(fields.get(PageField.NAME)).as("name file should be " + NAME).isEqualTo(NAME);
        assertThat(fields.get(PageField.DESCRIPTION)).as("description entry should be " + DESCRIPTION).isEqualTo(DESCRIPTION);

    }

    @Test
    public void pageCreatorWithDisplayName() throws Exception {
        // given
        PageCreator pageCreator = new PageCreator(NAME, "content.zip");
        pageCreator.setDisplayName(DISPLAY_NAME);

        // when
        Map<PageField, Serializable> fields = pageCreator.getFields();

        // then
        assertThat(fields).as("field size should contains name entry").hasSize(2).containsKey(PageField.NAME).containsKey(PageField.DISPLAY_NAME);
        assertThat(fields.get(PageField.NAME)).as("name file should be " + NAME).isEqualTo(NAME);
        assertThat(fields.get(PageField.DISPLAY_NAME)).as("display name entry should be " + DISPLAY_NAME).isEqualTo(DISPLAY_NAME);

    }


}
