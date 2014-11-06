package org.bonitasoft.engine.filter;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class AbstractUserFilterTest {

    private AbstractUserFilter abstractUserFilterTest;

    @Before
    public void setUp() {
        abstractUserFilterTest = new AbstractUserFilter() {

            @Override
            public void validateInputParameters() {

            }

            @Override
            public List<Long> filter(final String actorName) {
                return null;
            }

        };
    }

    @After
    public void tearDown() {
    }

    @Test
    public void should_getInputParameter_return_null_if_map_does_not_contain_key() throws Exception {
        assertThat(abstractUserFilterTest.getInputParameter("aInput")).isNull();
    }

}
