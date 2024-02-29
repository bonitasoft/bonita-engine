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
package org.bonitasoft.web.rest.server.datastore.bpm;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.util.HashMap;

import org.bonitasoft.test.toolkit.bpm.TestCase;
import org.bonitasoft.test.toolkit.bpm.TestProcessFactory;
import org.bonitasoft.test.toolkit.organization.TestUser;
import org.bonitasoft.test.toolkit.organization.TestUserFactory;
import org.bonitasoft.web.rest.model.bpm.cases.CommentItem;
import org.bonitasoft.web.rest.server.datastore.bpm.cases.CommentDatastore;
import org.bonitasoft.web.rest.server.framework.search.ItemSearchResult;
import org.bonitasoft.web.test.AbstractConsoleTest;
import org.junit.Test;

public class CommentDatastoreIntegrationIT extends AbstractConsoleTest {

    private CommentDatastore commentDatastore;

    @Override
    protected TestUser getInitiator() {
        return TestUserFactory.getJohnCarpenter();
    }

    @Override
    public void consoleTestSetUp() throws Exception {
        commentDatastore = new CommentDatastore(TestUserFactory.getJohnCarpenter().getSession());
    }

    private TestCase aCase() {
        return TestProcessFactory.getDefaultHumanTaskProcess().addActor(TestUserFactory.getJohnCarpenter()).startCase();
    }

    private HashMap<String, String> filterByCaseId(long caseId) {
        HashMap<String, String> filters = new HashMap<>();
        filters.put(CommentItem.ATTRIBUTE_PROCESS_INSTANCE_ID, String.valueOf(caseId));
        return filters;
    }

    @Test
    public void comment_datastore_can_do_a_paginated_search() throws Exception {
        TestCase aCase = aCase();
        aCase.addComment("Comment 1");
        aCase.addComment("Comment 2");
        aCase.addComment("Comment 3");

        final ItemSearchResult<CommentItem> results = commentDatastore.search(0, 2, null, null,
                filterByCaseId(aCase.getId()));

        assertThat(results.getTotal(), is(3L));
        assertThat(results.getResults().size(), is(2));
        assertThat(results.getResults().get(0).getContent(), is("Comment 1"));
        assertThat(results.getResults().get(1).getContent(), is("Comment 2"));
    }

    @Test
    public void comment_datastore_can_search_comments_with_special_characters() throws Exception {
        String specialCharComment = "#*Ã©Ã Ã¢Ã¤Ã«ÃªÃ©~Ã§ÃžÅ¡Å’Ã˜Ã�Ã†";
        TestCase aCase = aCase();
        aCase.addComment(specialCharComment);

        final ItemSearchResult<CommentItem> results = commentDatastore.search(0, 10, null, null,
                filterByCaseId(aCase.getId()));

        assertThat(results.getResults().get(0).getContent(), is(specialCharComment));
    }
}
