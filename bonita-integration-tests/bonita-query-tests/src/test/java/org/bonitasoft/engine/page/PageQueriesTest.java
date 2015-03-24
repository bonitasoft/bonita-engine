/**
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
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
package org.bonitasoft.engine.page;

import static org.assertj.core.api.Assertions.assertThat;
import static org.bonitasoft.engine.test.persistence.builder.PageBuilder.aPage;

import java.util.List;
import javax.inject.Inject;

import org.bonitasoft.engine.test.persistence.repository.PageRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "/testContext.xml" })
@Transactional
public class PageQueriesTest {

    @Inject
    private PageRepository repository;

    @Test
    public void getPageContent_should_return_the_content_of_the_page() {
        // given
        final SPage page = repository.add(aPage().withName("MyPage").withContent("The content".getBytes()).build());

        //when
        final SPageContent pageContent = repository.getPageContent(page.getId());
        // //then
        assertThat(pageContent.getContent()).isEqualTo("The content".getBytes());
        assertThat(pageContent.getId()).isEqualTo(page.getId());
    }

    @Test
    public void getPageByName_should_return_the_page_having_the_name() {
        // given
        repository.add(aPage().withName("MyPage1").withContent("The content".getBytes()).build());
        final SPage page2 = repository.add(aPage().withName("MyPage2").withContent("The content".getBytes()).build());

        //when
        final SPage pageByName = repository.getPageByName("MyPage2");

        // //then
        assertThat(pageByName.getId()).isEqualTo(page2.getId());
    }

    @Test
    public void getPageByNameAndProcessDefinition_should_return_the_page_having_the_name() {
        // given
        final SPageWithContent myPage1 = repository.add(aPage()
                .withName("MyPage1")
                .withProcessDefinitionId(1L)
                .build());
        assertThat(myPage1).as("should add the page").isNotNull();

        //when
        final SPage pageByName = repository.getPageByNameAndProcessDefinitionId("MyPage1", 1L);

        // //then
        assertThat(pageByName).as("should retrieve the page").isNotNull();
    }

    @Test
    public void getPageByProcessDefinition_should_filter_results() {
        // given
        final SPageWithContent myPage1 = repository.add(aPage()
                .withName("MyPage1")
                .withProcessDefinitionId(1L)
                .withContentType(ContentType.FORM)
                .build());
        final SPageWithContent myPage2 = repository.add(aPage()
                .withName("MyPage2")
                .withProcessDefinitionId(2L)
                .withContentType(ContentType.FORM)
                .build());

        assertThat(myPage1).as("should add the page").isNotNull();

        //when
        final List<SPage> results = repository.getPageByProcessDefinitionId(1L);

        // //then
        assertThat(results).as("should retrieve the page").hasSize(1);
        assertThat(results.get(0)).as("should retrieve the page").isEqualToComparingOnlyGivenFields(myPage1, "name", "processDefinitionId", "contentType");

    }

}
