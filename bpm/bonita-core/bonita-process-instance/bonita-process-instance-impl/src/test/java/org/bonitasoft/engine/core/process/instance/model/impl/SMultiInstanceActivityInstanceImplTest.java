package org.bonitasoft.engine.core.process.instance.model.impl;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Before;
import org.junit.Test;

public class SMultiInstanceActivityInstanceImplTest {

    private SMultiInstanceActivityInstanceImpl multi;

    @Before
    public void setUp() {
        multi = new SMultiInstanceActivityInstanceImpl();
    }

    @Test
    public void mustExecuteOnAbortOrCancelProcess_should_return_false_when_is_stable() {
        //given
        multi.setStable(true);

        //when
        final boolean executeOnAbortOrCancelProcess = multi.mustExecuteOnAbortOrCancelProcess();

        //then
        assertThat(executeOnAbortOrCancelProcess).isFalse();
    }

    @Test
    public void mustExecuteOnAbortOrCancelProcess_should_return_false_when_is_not_stable() {
        //given
        multi.setStable(false);

        //when
        final boolean executeOnAbortOrCancelProcess = multi.mustExecuteOnAbortOrCancelProcess();

        //then
        assertThat(executeOnAbortOrCancelProcess).isFalse();
    }

}
