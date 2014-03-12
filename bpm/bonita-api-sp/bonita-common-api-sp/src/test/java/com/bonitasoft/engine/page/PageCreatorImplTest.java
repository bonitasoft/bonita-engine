package com.bonitasoft.engine.page;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.Serializable;
import java.util.Map;

import org.junit.Test;

import com.bonitasoft.engine.page.PageCreator;
import com.bonitasoft.engine.page.PageCreator.PageField;

public class PageCreatorImplTest {

    private static final String NAME = "page name";

    private static final String DESCRIPTION = "page description";

    @Test
    public void pageCreatorWithName() throws Exception {
        // given
        PageCreator pageCreator = new PageCreator(NAME);

        // wwhen
        Map<PageField, Serializable> fields = pageCreator.getFields();

        // then
        assertThat(fields).as("field size should contains name entry").hasSize(1).containsKey(PageField.NAME).doesNotContainKey(PageField.DESCRIPTION);
        assertThat(fields.get(PageField.NAME)).as("name file shouls be").isEqualTo(NAME);

    }

    @Test
    public void pageCreatorWithNameAndDescrition() throws Exception {
        // given
        PageCreator pageCreator = new PageCreator(NAME);
        pageCreator.setDescription(DESCRIPTION);

        // when
        Map<PageField, Serializable> fields = pageCreator.getFields();

        // then
        assertThat(fields).as("field size should contains name entry").hasSize(2).containsKey(PageField.NAME).containsKey(PageField.DESCRIPTION);
        assertThat(fields.get(PageField.NAME)).as("name file should be " + NAME).isEqualTo(NAME);
        assertThat(fields.get(PageField.DESCRIPTION)).as("description entry should be " + DESCRIPTION).isEqualTo(DESCRIPTION);

    }

}
