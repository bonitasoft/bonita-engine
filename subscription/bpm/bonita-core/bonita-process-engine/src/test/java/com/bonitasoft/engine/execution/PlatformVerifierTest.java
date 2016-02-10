/**
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 **/

package com.bonitasoft.engine.execution;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.spy;

import com.bonitasoft.manager.Manager;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.platform.PlatformRetriever;
import org.bonitasoft.engine.platform.model.impl.SPlatformImpl;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class PlatformVerifierTest {

    @Mock
    private Manager manager;

    @Mock
    private TechnicalLoggerService technicalLoggerService;

    @Mock
    private PlatformRetriever platformRetriever;

    @InjectMocks
    private PlatformVerifier platformVerifier;

    private PlatformVerifier spyPlatformVerifier;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Before
    public void setUp() throws Exception {
        spyPlatformVerifier = spy(platformVerifier);
        given(spyPlatformVerifier.getManager()).willReturn(manager);
    }

    @Test
    public void check_should_throw_exception_when_manager_throws_exception() throws Throwable {
        //given
        SPlatformImpl platform = buildPlatform("info");
        given(platformRetriever.getPlatform()).willReturn(platform);
        given(manager.checkPlatformInfo("info")).willThrow(new IllegalStateException("error"));

        //then
        expectedException.expect(IllegalStateException.class);
        expectedException.expectMessage("error");

        //when
        spyPlatformVerifier.check();

    }

    private SPlatformImpl buildPlatform(final String initialInfo) {
        SPlatformImpl platform = new SPlatformImpl("7.0.0", "6.5.3", "6.4.0", "admin", System.currentTimeMillis());
        platform.setInformation(initialInfo);
        return platform;
    }

}