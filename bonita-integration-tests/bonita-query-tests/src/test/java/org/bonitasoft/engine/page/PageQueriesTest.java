/**
 * Copyright (C) 2019 Bonitasoft S.A.
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
package org.bonitasoft.engine.page;

import static org.assertj.core.api.Assertions.assertThat;
import static org.bonitasoft.engine.test.persistence.builder.PageBuilder.aPage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bonitasoft.engine.test.persistence.repository.PageRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

@RunWith(SpringRunner.class)
@ContextConfiguration(locations = { "/testContext.xml" })
@Transactional
public class PageQueriesTest {

    @Autowired
    private PageRepository repository;

    @Test
    public void getPageContent_should_return_the_content_of_the_page() {
        // given
        final AbstractSPage page = repository
                .add(aPage().withName("MyPage").withContent("The content".getBytes()).build());

        //when
        final SPageWithContent pageContent = repository.getPageContent(page.getId());
        // //then
        assertThat(pageContent.getContent()).isEqualTo("The content".getBytes());
        assertThat(pageContent.getId()).isEqualTo(page.getId());
    }

    @Test
    public void getPageByName_should_return_the_page_having_the_name() {
        // given
        repository.add(aPage().withName("MyPage1").withContent("The content".getBytes()).build());
        final AbstractSPage page2 = repository
                .add(aPage().withName("MyPage2").withContent("The content".getBytes()).build());

        //when
        final AbstractSPage pageByName = repository.getPageByName("MyPage2");

        // //then
        assertThat(pageByName.getId()).isEqualTo(page2.getId());
    }

    @Test
    public void should_getPageByName_return_the_page_without_processDefinitionId() {
        //given
        repository.add(aPage().withName("aPage").withContent("content of the global page".getBytes()).build());
        repository.add(aPage().withName("aPage").withContent("content of the process page 1".getBytes())
                .withProcessDefinitionId(123L).build());
        repository.add(aPage().withName("anOtherPage").withContent("content of the process page 2".getBytes())
                .withProcessDefinitionId(123L).build());
        //when
        SPage globalPage = repository.getPageByName("aPage");
        SPage processPage1 = repository.getPageByNameAndProcessDefinitionId("aPage", 123L);
        SPage processPage2 = repository.getPageByNameAndProcessDefinitionId("anOtherPage", 123L);
        //then
        assertThat(new String(repository.getPageContent(globalPage.getId()).getContent()))
                .isEqualTo("content of the global page");
        assertThat(new String(repository.getPageContent(processPage1.getId()).getContent()))
                .isEqualTo("content of the process page 1");
        assertThat(new String(repository.getPageContent(processPage2.getId()).getContent()))
                .isEqualTo("content of the process page 2");
    }

    @Test
    public void getPageByNameAndProcessDefinition_should_return_the_page_having_the_name() {
        // given
        final AbstractSPage myPage1 = repository.add(aPage()
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
        final AbstractSPage myPage1 = repository.add(aPage()
                .withName("MyPage1")
                .withProcessDefinitionId(1L)
                .withContentType(ContentType.FORM)
                .build());
        repository.add(aPage()
                .withName("AnotherPage")
                .withProcessDefinitionId(2L)
                .withContentType(ContentType.FORM)
                .build());

        assertThat(myPage1).as("should add the page").isNotNull();

        //when
        final List<SPage> results = repository.getPageByProcessDefinitionId(1L);

        // //then
        assertThat(results).as("should retrieve the page").hasSize(1);
        assertThat(results.get(0).getName()).as("should retrieve the right page").isEqualTo(myPage1.getName());
        assertThat(results.get(0).getProcessDefinitionId()).as("should retrieve the right page")
                .isEqualTo(myPage1.getProcessDefinitionId());
        assertThat(results.get(0).getContentType()).as("should retrieve the right page")
                .isEqualTo(myPage1.getContentType());
    }

    @Test
    public void should_retrieve_SPageMapping_previously_saved() {
        // given:
        final List<String> authorizationRules = new ArrayList<>(Arrays.asList("rule1", "rule2", "rule3"));
        SPageMapping pageMapping = SPageMapping.builder().pageId(2L).key("myKey").url("http://www/example.com")
                .urlAdapter("legacy").build();
        pageMapping.setPageAuthorizationRules(authorizationRules);
        repository.add(pageMapping);

        // when:
        final SPageMapping mapping = repository.getPageMappingByKey("myKey");

        // then:
        assertThat(mapping).extracting("pageId", "key", "url", "urlAdapter")
                .containsOnly(2L, "myKey", "http://www/example.com", "legacy");
        assertThat(mapping.getPageAuthorizationRules()).isEqualTo(authorizationRules);
    }
}
