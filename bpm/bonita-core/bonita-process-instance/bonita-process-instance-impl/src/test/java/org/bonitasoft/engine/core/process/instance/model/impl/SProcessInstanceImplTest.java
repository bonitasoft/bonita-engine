package org.bonitasoft.engine.core.process.instance.model.impl;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

public class SProcessInstanceImplTest {

    @Test
    public void defaultInterruptingEventIdShouldBeMinusOne() {
        assertThat(new SProcessInstanceImpl().getInterruptingEventId()).isEqualTo(-1L);
    }
}
