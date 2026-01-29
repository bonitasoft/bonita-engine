/**
 * Copyright (C) 2026 Bonitasoft S.A.
 * Bonitasoft, 32 rue Gustave Eiffel - 38000 Grenoble
 * This library is free software; you can redistribute it and/or modify it under the terms
 * of the GNU Lesser General Public License as published by the Free Software Foundation
 * version 2.1 of the License.
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License along with this
 * program; if not, write to the Free Software Foundation, Inc., 51 Franklin Street, Fifth
 * Floor, Boston, MA 02110-1301, USA.
 **/
package org.bonitasoft.web.rest.server.api;

import java.util.HashMap;
import java.util.Map;

import org.bonitasoft.console.common.server.login.servlet.PlatformLoginServlet;
import org.bonitasoft.engine.session.PlatformSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

/**
 * Abstract base class for Spring MVC controller tests that use PlatformAPI.
 * Provides common MockMvc setup with PlatformSession and platform API mocking infrastructure.
 * <p>
 * This base class eliminates 15-20 lines of repetitive test setup boilerplate.
 * <p>
 * <b>Differences from AbstractControllerTest:</b>
 * <ul>
 * <li>Uses PlatformSession instead of APISession</li>
 * <li>Uses MockMvcBuilders.standaloneSetup() without exception handler</li>
 * <li>Configures PlatformSession in sessionAttributes directly</li>
 * </ul>
 * <p>
 * <b>Usage example:</b>
 *
 * <pre>
 * class LicenseInfoControllerTest extends AbstractPlatformControllerTest&lt;LicenseInfoController&gt; {
 *
 *     &#64;Mock
 *     protected PlatformAPI platformAPI;
 *
 *     &#64;Override
 *     protected LicenseInfoController createController() {
 *         return spy(new LicenseInfoController());
 *     }
 *
 *     &#64;Override
 *     protected void configureMocks(LicenseInfoController controller) throws Exception {
 *         doReturn(platformAPI).when(controller).getPlatformAPI(any(PlatformSession.class));
 *     }
 *
 *     &#64;Test
 *     void should_return_license_info() throws Exception {
 *         // Test implementation (no setup boilerplate!)
 *         when(platformAPI.getInformation()).thenReturn(mockInfo);
 *
 *         mockMvc.perform(get("/API/platform/license")
 *                 .sessionAttrs(sessionAttributes))
 *                 .andExpect(status().isOk());
 *     }
 * }
 * </pre>
 *
 * @param <C> the controller type being tested (must extend AbstractRESTController)
 * @see AbstractControllerTest for standard API controller tests
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public abstract class AbstractPlatformControllerTest<C extends AbstractRESTController> {

    /**
     * MockMvc instance for performing HTTP requests in tests.
     * Configured with standalone setup (no global exception handler).
     */
    protected MockMvc mockMvc;

    /**
     * Session attributes map containing the PlatformSession.
     * Use this in mockMvc.perform() calls: .sessionAttrs(sessionAttributes)
     */
    protected final Map<String, Object> sessionAttributes = new HashMap<>();

    /**
     * Mocked PlatformSession for platform API access.
     * Automatically configured in setUp().
     */
    @Mock
    protected PlatformSession platformSession;

    /**
     * Sets up MockMvc and configures platform API mocks before each test.
     * Calls createController() and configureMocks() to allow test customization.
     */
    @BeforeEach
    void setUp() throws Exception {
        C controller = createController();
        sessionAttributes.put(PlatformLoginServlet.PLATFORM_SESSION_PARAM_KEY, platformSession);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
        configureMocks(controller);
    }

    /**
     * Creates the controller instance to be tested.
     * Typically returns spy(new YourController()) to allow mocking of API accessor methods.
     *
     * @return the controller instance
     */
    protected abstract C createController();

    /**
     * Configures mock platform API dependencies for the controller.
     * Called after MockMvc setup, before tests run.
     * <p>
     * Example:
     *
     * <pre>
     *
     * protected void configureMocks(MyController controller) throws Exception {
     *     doReturn(platformAPI).when(controller).getPlatformAPI(any(PlatformSession.class));
     * }
     * </pre>
     *
     * @param controller the controller instance (same as returned by createController())
     * @throws Exception if mock configuration fails
     */
    protected abstract void configureMocks(C controller) throws Exception;
}
