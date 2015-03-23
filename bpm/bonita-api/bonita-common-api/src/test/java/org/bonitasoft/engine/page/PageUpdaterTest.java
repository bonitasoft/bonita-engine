package org.bonitasoft.engine.page;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;

import java.io.Serializable;
import java.util.Map;

import org.junit.Test;

public class PageUpdaterTest {

    private static final String ZIP_FILE_NAME = "content.zip";

    private static final String DISPLAY_NAME = "display name";

    private static final String NAME = "page name";

    private static final String DESCRIPTION = "page description";
    public static final long PROCESS_DEFINITION_ID = 1L;

    @Test
    public void should_create_page_with_default_fields() {
        // given
        final PageUpdater pageUpdater = new PageUpdater().setName(NAME).setContentName(ZIP_FILE_NAME).setDisplayName(DISPLAY_NAME).setDescription(DESCRIPTION)
                .setContentType(ContentType.FORM).setProcessDefinitionId(PROCESS_DEFINITION_ID);

        // when
        final Map<PageUpdater.PageUpdateField, Serializable> fields = pageUpdater.getFields();

        // then
        assertThat(fields).as("should set content type").containsOnly(entry(PageUpdater.PageUpdateField.NAME, NAME),
                entry(PageUpdater.PageUpdateField.DISPLAY_NAME, DISPLAY_NAME),
                entry(PageUpdater.PageUpdateField.DESCRIPTION, DESCRIPTION),
                entry(PageUpdater.PageUpdateField.CONTENT_TYPE, ContentType.FORM),
                entry(PageUpdater.PageUpdateField.CONTENT_NAME, ZIP_FILE_NAME),
                entry(PageUpdater.PageUpdateField.PROCESS_DEFINITION_ID, PROCESS_DEFINITION_ID),
                entry(PageUpdater.PageUpdateField.NAME, NAME)
                );
    }

}
