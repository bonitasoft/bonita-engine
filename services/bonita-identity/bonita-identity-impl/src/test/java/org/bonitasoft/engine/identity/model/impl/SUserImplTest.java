package org.bonitasoft.engine.identity.model.impl;

import static org.assertj.core.api.Assertions.assertThat;

import org.bonitasoft.engine.identity.model.impl.SUserImpl;
import org.junit.Test;

public class SUserImplTest {

    private static final String EXPECTED_TO_STRING = "SUserImpl (0) [firstName=null, lastName=null, userName=walter.bates, iconName=null, iconPath=null, managerUserId=0, delegeeUserName=null, title=null, jobTitle=null, creationDate=0, createdBy=0, lastUpdate=0, lastConnection=null, enabled=false]";

    @Test
    public void toString_should_not_display_password() throws Exception {
        //given
        final SUserImpl sUserImpl = new SUserImpl();
        sUserImpl.setUserName("walter.bates");
        sUserImpl.setPassword("bpm");

        //when
        final String string = sUserImpl.toString();

        //then
        assertThat(string).as("should not display password!").isEqualTo(EXPECTED_TO_STRING).doesNotContain("password");
    }
}
