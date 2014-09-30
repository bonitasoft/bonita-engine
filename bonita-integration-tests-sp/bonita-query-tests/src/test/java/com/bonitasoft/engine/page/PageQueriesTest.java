/*******************************************************************************
 * Copyright (C) 2014 Bonitasoft S.A.
 * Bonitasoft is a trademark of Bonitasoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * Bonitasoft, 32 rue Gustave Eiffel 38000 Grenoble
 * or Bonitasoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.page;

import static com.bonitasoft.engine.test.persistence.builder.PageBuilder.aPage;
import static org.assertj.core.api.Assertions.assertThat;

import javax.inject.Inject;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import com.bonitasoft.engine.test.persistence.repository.PageRepository;

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
        //
        // //when
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
        //
        // //when
        final SPage pageByName = repository.getPageByName("MyPage2");

        // //then
        assertThat(pageByName.getId()).isEqualTo(page2.getId());
    }

}
