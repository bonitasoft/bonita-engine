package org.bonitasoft.engine.core.process.instance.model.event.handling;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

public class SMessageInstanceTest {

    @Test
    public void should_have_a_creation_date() {
        SMessageInstance sMessageInstance = new SMessageInstance("myMessage", "target", "target", 1234L, "fn");

        assertThat(sMessageInstance.getCreationDate()).isGreaterThan(0);
    }

}