package org.bonitasoft.engine.identity.model.impl;

import static org.assertj.core.api.Assertions.assertThat;

import org.bonitasoft.engine.identity.model.impl.SUserImpl;
import org.junit.Test;

public class SUserImplTest {


    @Test
    public void toString_should_not_display_password() throws Exception {
        //given
        final SUserImpl sUserImpl = new SUserImpl();
        sUserImpl.setUserName("walter.bates");
        sUserImpl.setPassword("bpm");

        //when
        final String string = sUserImpl.toString();

        //then
        assertThat(string).as("should not display password!").doesNotContain("password").doesNotContain("bpm");
    }
}
