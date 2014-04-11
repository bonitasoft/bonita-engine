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
    public void getPageContent_should_return_the_content_of_the_page() throws Exception {
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
    public void getPageByName_should_return_the_page_having_the_name() throws Exception {
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
