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

import static org.bonitasoft.web.rest.server.api.RestControllerUtils.initMockMvcWithSessionAttributes;

import java.util.HashMap;
import java.util.Map;

import org.bonitasoft.engine.session.APISession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Abstract base class for Spring MVC controller tests.
 * Provides common MockMvc setup with session attributes and API mocking infrastructure.
 * <p>
 * This base class eliminates 15-20 lines of repetitive test setup boilerplate.
 * <p>
 * <b>Usage example:</b>
 *
 * <pre>
 * class MyControllerTest extends AbstractControllerTest&lt;MyController&gt; {
 *
 *     &#64;Mock
 *     protected ProcessAPI processAPI;
 *
 *     &#64;Override
 *     protected MyController createController() {
 *         return spy(new MyController());
 *     }
 *
 *     &#64;Override
 *     protected void configureMocks(MyController controller) throws Exception {
 *         doReturn(processAPI).when(controller).getProcessAPI(apiSession);
 *     }
 *
 *     &#64;Test
 *     void should_test_something() throws Exception {
 *         // Test implementation (no setup boilerplate!)
 *         mockMvc.perform(get("/API/endpoint")
 *                 .sessionAttrs(sessionAttributes))
 *                 .andExpect(status().isOk());
 *     }
 * }
 * </pre>
 *
 * @param <C> the controller type being tested (must extend AbstractRESTController)
 * @see AbstractPlatformControllerTest for PlatformAPI controller tests
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public abstract class AbstractControllerTest<C extends AbstractRESTController> {

    /**
     * MockMvc instance for performing HTTP requests in tests.
     * Configured with session attributes and exception handling.
     */
    protected MockMvc mockMvc;

    /**
     * Session attributes map containing the APISession.
     * Use this in mockMvc.perform() calls: .sessionAttrs(sessionAttributes)
     */
    protected final Map<String, Object> sessionAttributes = new HashMap<>();

    /**
     * Mocked APISession for standard API access.
     * Automatically configured in setUp().
     */
    @Mock
    protected APISession apiSession;

    /**
     * Sets up MockMvc and configures API mocks before each test.
     * Calls createController() and configureMocks() to allow test customization.
     */
    @BeforeEach
    void setUp() throws Exception {
        C controller = createController();
        mockMvc = initMockMvcWithSessionAttributes(controller, sessionAttributes, apiSession);
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
     * Configures mock API dependencies for the controller.
     * Called after MockMvc setup, before tests run.
     * <p>
     * Example:
     *
     * <pre>
     *
     * protected void configureMocks(MyController controller) throws Exception {
     *     doReturn(processAPI).when(controller).getProcessAPI(apiSession);
     *     doReturn(identityAPI).when(controller).getIdentityAPI(apiSession);
     * }
     * </pre>
     *
     * @param controller the controller instance (same as returned by createController())
     * @throws Exception if mock configuration fails
     */
    protected abstract void configureMocks(C controller) throws Exception;
}
