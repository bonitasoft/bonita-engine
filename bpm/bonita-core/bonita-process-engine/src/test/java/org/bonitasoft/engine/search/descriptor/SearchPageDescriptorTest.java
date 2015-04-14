package org.bonitasoft.engine.search.descriptor;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;
import java.util.Set;

import org.bonitasoft.engine.page.SPage;
import org.bonitasoft.engine.persistence.PersistentObject;
import org.junit.Test;

public class SearchPageDescriptorTest {

    @Test
    public void should_get_all_page_fields() throws Exception {
        //given
        SearchPageDescriptor searchPageDescriptor = new SearchPageDescriptor();

        //when
        final Map<Class<? extends PersistentObject>, Set<String>> allFields = searchPageDescriptor.getAllFields();

        //then
        final Set<String> fields = allFields.get(SPage.class);
        assertThat(fields).contains("name", "displayName");

    }

    @Test
    public void should_get_all_page_keys() throws Exception {
        //given
        SearchPageDescriptor searchPageDescriptor = new SearchPageDescriptor();

        //when
        final Map<String, FieldDescriptor> entityKeys = searchPageDescriptor.getEntityKeys();

        //then
        assertThat(entityKeys).containsKeys("contentType", "processDefinitionId");

    }

}
