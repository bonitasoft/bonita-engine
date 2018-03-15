package org.bonitasoft.platform.setup;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.ExpectedSystemExit;
import org.junit.contrib.java.lang.system.SystemErrRule;
import org.junit.contrib.java.lang.system.SystemOutRule;

/**
 * @author Laurent Leseigneur
 */
public class PlatformSetupApplicationTest {

    @Rule
    public SystemErrRule systemErrRule = new SystemErrRule().enableLog().muteForSuccessfulTests();
    @Rule
    public SystemOutRule systemOutRule = new SystemOutRule().enableLog().muteForSuccessfulTests();

    @Rule
    public ExpectedSystemExit systemExit = ExpectedSystemExit.none();

    @Test
    public void should_gracefully_fail_with_error_message_when_null_action() {
        //then
        systemExit.expectSystemExitWithStatus(1);

        //when
        PlatformSetupApplication.main(null);
    }

    @Test
    public void should_gracefully_fail_with_error_message_when_action_unknown() {
        systemExit.expectSystemExitWithStatus(1);
        systemExit.checkAssertionAfterwards(() -> assertThat(systemOutRule.getLog()).contains("no command named: wrong value"));

        PlatformSetupApplication.main(new String[] { "wrong value" });
    }

    @Test
    public void main_should_accept_configure_action() {
        systemExit.expectSystemExitWithStatus(0);

        PlatformSetupApplication.main(new String[] { "configure" });
    }

    @Test
    public void should_show_error_if_no_command_specified() {
        //then
        systemExit.expectSystemExitWithStatus(1);
        systemExit.checkAssertionAfterwards(() -> {
            assertThat(systemOutRule.getLog()).contains("Need to specify a command, see usage above.");
            assertThat(systemOutRule.getLog()).contains("usage: setup ( init | configure | pull | push ) [-D <property=value>]");
        });
        //when
        PlatformSetupApplication.main(new String[] {});
    }

    @Test
    public void should_show_help_of_a_command() {
        //then
        systemExit.expectSystemExitWithStatus(0);
        systemExit.checkAssertionAfterwards(() -> assertThat(systemOutRule.getLog()).contains("Run this init command once to create database structure"));
        //when
        PlatformSetupApplication.main(new String[] { "help", "init" });
    }

}
