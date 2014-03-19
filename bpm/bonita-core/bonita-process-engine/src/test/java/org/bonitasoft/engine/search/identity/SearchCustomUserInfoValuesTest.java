package org.bonitasoft.engine.search.identity;

import java.util.Arrays;
import java.util.List;

import org.bonitasoft.engine.api.impl.DummySCustomUserInfoValue;
import org.bonitasoft.engine.identity.CustomUserInfoValue;
import org.bonitasoft.engine.identity.IdentityService;
import org.bonitasoft.engine.identity.model.SCustomUserInfoValue;
import org.bonitasoft.engine.search.SearchOptions;
import org.bonitasoft.engine.search.descriptor.SearchEntityDescriptor;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Vincent Elcrin
 */
@RunWith(MockitoJUnitRunner.class)
public class SearchCustomUserInfoValuesTest {

    @Mock
    private IdentityService service;

    @Mock
    private SearchEntityDescriptor descriptor;

    @Mock
    private SearchOptions options;

    @Test
    public void should_return_a_list_of_CustomUserInfoValues() throws Exception {
        SearchCustomUserInfoValues search = new SearchCustomUserInfoValues(service, descriptor, options);

        List<CustomUserInfoValue> result = search.convertToClientObjects(Arrays.<SCustomUserInfoValue>asList(
                new DummySCustomUserInfoValue(1L),
                new DummySCustomUserInfoValue(2L)));

        assertThat(result.get(0).getDefinitionId()).isEqualTo(1L);
        assertThat(result.get(1).getDefinitionId()).isEqualTo(2L);
    }
}
