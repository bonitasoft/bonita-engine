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
package org.bonitasoft.web.rest.server.api.system;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.bonitasoft.engine.api.MaintenanceAPI;
import org.bonitasoft.engine.maintenance.MaintenanceDetails;
import org.bonitasoft.engine.maintenance.impl.MaintenanceDetailsImpl;
import org.bonitasoft.web.rest.server.api.AbstractControllerTest;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.http.MediaType;

/**
 * BPA-833: {@code MaintenanceController} returns {@link MaintenanceDetails} directly, with no DTO and no custom
 * serializer, so {@code MaintenanceDetailsImpl} is the one engine bean that reaches a REST response through
 * Jackson raw. Adding the {@code getMaintenanceMessageActive()} alias next to {@code isMaintenanceMessageActive()}
 * therefore changes what Jackson sees on a serialized type.
 * <p>
 * It does not change the payload: Jackson already accepted an {@code isX()} returning a wrapper {@code Boolean} as
 * a getter, so the property was present before, and the alias derives the same implicit name rather than raising a
 * conflict. That is Jackson behaviour rather than a documented guarantee, so the response body is pinned here -
 * the existing {@code MaintenanceControllerTest} only asserts the controller's return value, never the JSON.
 */
class MaintenanceControllerJsonTest extends AbstractControllerTest<MaintenanceController> {

    @Mock
    protected MaintenanceAPI maintenanceAPI;

    @Override
    protected MaintenanceController createController() {
        return spy(new MaintenanceController());
    }

    @Override
    protected void configureMocks(MaintenanceController controller) throws Exception {
        doReturn(maintenanceAPI).when(controller).getMaintenanceAPI(apiSession);
    }

    @Test
    void getMaintenanceDetails_should_serialize_maintenanceMessageActive_once() throws Exception {
        //given
        doReturn(MaintenanceDetailsImpl.builder()
                .maintenanceState(MaintenanceDetails.State.ENABLED)
                .maintenanceMessage("back soon")
                .maintenanceMessageActive(true)
                .build()).when(maintenanceAPI).getMaintenanceDetails();

        //when-then strict mode: any duplicated or extra property fails the comparison
        mockMvc.perform(get("/API/system/maintenance").sessionAttrs(sessionAttributes)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(content().json("""
                        {
                            "maintenanceState": "ENABLED",
                            "maintenanceMessage": "back soon",
                            "maintenanceMessageActive": true
                        }""", true));
    }

    /**
     * The response type backs the property with a primitive {@code boolean}, so an unset activation serializes as
     * {@code false} rather than {@code null}. Only the request DTO {@code MaintenanceDetailsClient} declares a
     * nullable {@code Boolean}, which is what {@code MaintenanceController} tests for {@code != null} to implement
     * partial updates - a distinction worth keeping visible, since it is why {@code MaintenanceDetails} cannot be
     * narrowed to a primitive the way the server-internal types were.
     */
    @Test
    void getMaintenanceDetails_should_serialize_an_unset_maintenanceMessageActive_as_false() throws Exception {
        //given
        doReturn(MaintenanceDetailsImpl.builder()
                .maintenanceState(MaintenanceDetails.State.DISABLED)
                .build()).when(maintenanceAPI).getMaintenanceDetails();

        //when-then
        mockMvc.perform(get("/API/system/maintenance").sessionAttrs(sessionAttributes)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json("""
                        {
                            "maintenanceState": "DISABLED",
                            "maintenanceMessage": null,
                            "maintenanceMessageActive": false
                        }""", true));
    }

}
