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
package org.bonitasoft.web.rest.server.api.bpm.cases;

import static org.junit.Assert.assertEquals;

import java.util.HashMap;

import org.bonitasoft.test.toolkit.bpm.TestCase;
import org.bonitasoft.test.toolkit.bpm.TestProcessFactory;
import org.bonitasoft.test.toolkit.organization.TestUser;
import org.bonitasoft.test.toolkit.organization.TestUserFactory;
import org.bonitasoft.web.rest.model.bpm.cases.ArchivedCommentItem;
import org.bonitasoft.web.rest.server.framework.search.ItemSearchResult;
import org.bonitasoft.web.test.AbstractConsoleTest;
import org.junit.Test;

/**
 * @author Paul AMAR
 */
public class APIArchivedCommentIT extends AbstractConsoleTest {

    private APIArchivedComment apiArchivedComment;

    @Override
    public void consoleTestSetUp() throws Exception {
        this.apiArchivedComment = new APIArchivedComment();
        this.apiArchivedComment
                .setCaller(getAPICaller(TestUserFactory.getJohnCarpenter().getSession(), "API/bpm/archivedComment"));
    }

    @Override
    protected TestUser getInitiator() {
        return TestUserFactory.getJohnCarpenter();
    }

    @Test
    public void testSearch() throws Exception {
        TestCase aCase = TestProcessFactory.getDefaultHumanTaskProcess().addActor(getInitiator()).startCase();
        aCase.getNextHumanTask().assignTo(getInitiator());
        aCase.addComments(getInitiator(), 12, "mon Commentaire");
        aCase.execute();

        final ItemSearchResult<ArchivedCommentItem> mesResultats = this.apiArchivedComment.search(0, 12, "", "",
                new HashMap<>());

        assertEquals(mesResultats.getLength(), 12);
    }

}
