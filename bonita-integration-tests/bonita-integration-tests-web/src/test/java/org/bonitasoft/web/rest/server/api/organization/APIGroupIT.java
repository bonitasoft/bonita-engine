/**
 * Copyright (C) 2022 Bonitasoft S.A.
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
package org.bonitasoft.web.rest.server.api.organization;

import static org.bonitasoft.web.rest.model.builder.organisation.GroupItemBuilder.aGroup;
import static org.mockito.Mockito.spy;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;

import org.bonitasoft.engine.api.PlatformAPIAccessor;
import org.bonitasoft.engine.exception.BonitaHomeNotSetException;
import org.bonitasoft.engine.exception.ServerAPIException;
import org.bonitasoft.engine.exception.UnknownAPITypeException;
import org.bonitasoft.engine.io.FileContent;
import org.bonitasoft.test.toolkit.organization.TestUser;
import org.bonitasoft.test.toolkit.organization.TestUserFactory;
import org.bonitasoft.web.rest.model.identity.GroupItem;
import org.bonitasoft.web.test.AbstractConsoleTest;
import org.bonitasoft.web.toolkit.client.common.exception.api.APIException;
import org.bonitasoft.web.toolkit.client.data.APIID;
import org.junit.Assert;
import org.junit.Test;

public class APIGroupIT extends AbstractConsoleTest {

    private APIGroup apiGroup;

    @Override
    public void consoleTestSetUp() throws Exception {
        apiGroup = spy(new APIGroup());
        apiGroup.setCaller(getAPICaller(getInitiator().getSession(), "API/identity/group"));
    }

    @Override
    protected TestUser getInitiator() {
        return TestUserFactory.getJohnCarpenter();
    }

    @Test(expected = APIException.class)
    public void addingTwiceSameGroupIsForbidden() {
        final GroupItem groupItem = aGroup().build();

        apiGroup.add(groupItem);
        apiGroup.add(groupItem);
    }

    @Test
    public void should_update_group_icon()
            throws ServerAPIException, BonitaHomeNotSetException, UnknownAPITypeException, IOException {
        GroupItem input = new GroupItem();
        input.setName("Developper");
        input.setDescription("The guys who drink a lot of coffee");
        input = apiGroup.runAdd(input);
        final APIID id = input.getId();
        Assert.assertNotNull("Failed to add a new role", input);
        input = new GroupItem();

        //store icon into database
        File file = File.createTempFile("tmp", ".png");
        Files.writeString(file.toPath(), "content");
        String iconFileKey = PlatformAPIAccessor.getTemporaryContentAPI()
                .storeTempFile(new FileContent("icon.png", new FileInputStream(file), "img/png"));

        input.setIcon(iconFileKey);
        input = apiGroup.runUpdate(id, input.getAttributes());
        Assert.assertNotNull("Failed while updating the group", input);
    }

}
