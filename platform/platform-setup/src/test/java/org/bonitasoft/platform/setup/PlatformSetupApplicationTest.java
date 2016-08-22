package org.bonitasoft.platform.setup;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.Assertion;
import org.junit.contrib.java.lang.system.ClearSystemProperties;
import org.junit.contrib.java.lang.system.ExpectedSystemExit;
import org.junit.contrib.java.lang.system.SystemErrRule;

/**
 * @author Laurent Leseigneur
 */
public class PlatformSetupApplicationTest {

    @Rule
    public ClearSystemProperties clearSystemProperties = new ClearSystemProperties(PlatformSetup.BONITA_SETUP_ACTION);

    @Rule
    public SystemErrRule systemErrRule = new SystemErrRule().enableLog().muteForSuccessfulTests();

    @Rule
    public ExpectedSystemExit systemExit = ExpectedSystemExit.none();

    @Test
    public void should_accept_null_args() throws Exception {
        //then
        systemExit.expectSystemExitWithStatus(1);
        systemExit.checkAssertionAfterwards(new Assertion() {

            @Override
            public void checkAssertion() throws Exception {
                assertThat(systemErrRule.getLog().trim()).isEqualTo("ERROR: unknown argument value for 'action': null");
            }
        });

        //when
        PlatformSetupApplication.main(null);

    }

    @Test
    public void should_accept_empty_args() throws Exception {
        //given
        System.setProperty(PlatformSetup.BONITA_SETUP_ACTION, "wrong value");

        //then
        systemExit.expectSystemExitWithStatus(1);
        systemExit.checkAssertionAfterwards(new Assertion() {

            @Override
            public void checkAssertion() throws Exception {
                assertThat(systemErrRule.getLog().trim()).isEqualTo("ERROR: unknown argument value for 'action': wrong value");
            }
        });

        //when
        PlatformSetupApplication.main(null);

    }

}
