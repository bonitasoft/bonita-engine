package org.bonitasoft.engine.core.login;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import org.bonitasoft.engine.authentication.AuthenticationService;
import org.bonitasoft.engine.identity.IdentityService;
import org.bonitasoft.engine.session.SessionService;
import org.bonitasoft.engine.sessionaccessor.SessionAccessor;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class SecuredLoginServiceImplTest {

    @Mock
    private AuthenticationService authenticationService;

    @Mock
    private IdentityService identityService;

    @Mock
    private SessionAccessor sessionAccessor;

    @Mock
    private SessionService sessionService;

    private SecuredLoginServiceImpl securedLoginServiceImpl;

    @Before
    public void before() {
        securedLoginServiceImpl = spy(new SecuredLoginServiceImpl(authenticationService, sessionService, sessionAccessor, identityService));
    }

    @Test
    public void should_login_with_technical_user__return_technical_session() throws Exception {
        doReturn(new TechnicalUser("john", "bpm")).when(securedLoginServiceImpl).getTechnicalUser(1);

        securedLoginServiceImpl.login(1, "john", "bpm");

        verify(sessionService).createSession(1, -1, "john", true);
    }

}
