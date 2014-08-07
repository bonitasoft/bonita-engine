/**
 * Copyright (C) 2014 BonitaSoft S.A.
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
package com.bonitasoft.engine.application;

import static com.bonitasoft.engine.test.persistence.builder.ApplicationBuilder.anApplication;
import static com.bonitasoft.engine.test.persistence.builder.ApplicationPageBuilder.anApplicationPage;
import static com.bonitasoft.engine.test.persistence.builder.PageBuilder.aPage;
import static org.assertj.core.api.Assertions.assertThat;

import javax.inject.Inject;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import com.bonitasoft.engine.business.application.SApplication;
import com.bonitasoft.engine.business.application.SApplicationPage;
import com.bonitasoft.engine.business.application.impl.SApplicationImpl;
import com.bonitasoft.engine.page.SPage;
import com.bonitasoft.engine.test.persistence.repository.ApplicationRepository;

/**
 * @author Elias Ricken de Medeiros
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "/testContext.xml" })
@Transactional
public class ApplicationQueriesTest {

    @Inject
    private ApplicationRepository repository;

    @Test
    public void getApplicationByName_returns_the_application_with_the_given_name() throws Exception {
        //given
        repository.add(anApplication().withName("app1").withVersion("1.0").withPath("app1").build());
        final SApplication application2 = repository.add(anApplication().withName("app2").withVersion("1.0").withPath("app1").build());
        repository.add(anApplication().withName("app3").withVersion("1.0").withPath("app1").build());

        //when
        final SApplication retrievedApp = repository.getApplicationByName("app2");

        //then
        assertThat(retrievedApp).isEqualTo(application2);
    }

    @Test
    public void getApplication_returns_the_application_with_the_given_id() throws Exception {
        //given
        repository.add(anApplication().withName("app1").withVersion("1.0").withPath("app1").build());
        final SApplication application2 = repository.add(anApplication().withName("app2").withVersion("1.0").withPath("app1").build());
        repository.add(anApplication().withName("app3").withVersion("1.0").withPath("app1").build());

        //when
        final SApplication retrievedApp = repository.getApplication(application2.getId());

        //then
        assertThat(retrievedApp).isEqualTo(application2);
    }

    @Test
    public void getApplicationPageById_should_return_the_applicationPage_identified_by_the_given_id() throws Exception {
        //given
        final SApplication application1 = repository.add(anApplication().withName("app1").withVersion("1.0").withPath("/app1").build());
        final SPage page = repository.add(aPage().withName("MyPage").withContent("The content".getBytes()).build());
        repository.add(anApplicationPage().withName("FirstPage").withApplicationId(application1.getId()).withPageId(page.getId()).build());
        final SApplicationPage secondPageApp = repository.add(anApplicationPage().withName("SecondPage").withApplicationId(application1.getId())
                .withPageId(page.getId()).build());
        repository.add(anApplicationPage().withName("ThirdPage").withApplicationId(application1.getId()).withPageId(page.getId()).build());

        //when
        final SApplicationPage retrievedAppPage = repository.getApplicationPage(secondPageApp.getId());

        //then
        assertThat(retrievedAppPage).isEqualTo(secondPageApp);
    }

    @Test
    public void getApplicationPageByNameAnApplicationName_should_return_the_applicationPage_with_the_given_name_in_the_given_application() throws Exception {
        //given
        final SApplication application1 = repository.add(anApplication().withName("app1").withVersion("1.0").withPath("/app1").build());
        final SApplication application2 = repository.add(anApplication().withName("app2").withVersion("1.0").withPath("/app2").build());
        final SPage page = repository.add(aPage().withName("MyPage").withContent("The content".getBytes()).build());
        repository.add(anApplicationPage().withName("FirstPage").withApplicationId(application1.getId()).withPageId(page.getId()).build());
        final SApplicationPage secondPageApp1 = repository.add(anApplicationPage().withName("SecondPage").withApplicationId(application1.getId())
                .withPageId(page.getId()).build());
        repository.add(anApplicationPage().withName("FirstPage").withApplicationId(application2.getId()).withPageId(page.getId()).build());
        repository.add(anApplicationPage().withName("SecondPage").withApplicationId(application2.getId()).withPageId(page.getId()).build());

        //when
        final SApplicationPage retrievedAppPage = repository.getApplicationPageByNameAndApplicationName("app1", "SecondPage");

        //then
        assertThat(retrievedAppPage).isEqualTo(secondPageApp1);
    }

    @Test
    public void getApplicationPageByNameAndApplicationId_should_return_the_applicationPage_with_the_given_name_in_the_given_application() throws Exception {
        //given
        final SApplication application1 = repository.add(anApplication().withName("app1").withVersion("1.0").withPath("/app1").build());
        final SApplication application2 = repository.add(anApplication().withName("app2").withVersion("1.0").withPath("/app2").build());
        final SPage page = repository.add(aPage().withName("MyPage").withContent("The content".getBytes()).build());
        repository.add(anApplicationPage().withName("FirstPage").withApplicationId(application1.getId()).withPageId(page.getId()).build());
        final SApplicationPage secondPageApp1 = repository.add(anApplicationPage().withName("SecondPage").withApplicationId(application1.getId())
                .withPageId(page.getId()).build());
        repository.add(anApplicationPage().withName("FirstPage").withApplicationId(application2.getId()).withPageId(page.getId()).build());
        repository.add(anApplicationPage().withName("SecondPage").withApplicationId(application2.getId()).withPageId(page.getId()).build());

        //when
        final SApplicationPage retrievedAppPage = repository.getApplicationPageByNameAndApplicationId(application1.getId(), "SecondPage");

        //then
        assertThat(retrievedAppPage).isEqualTo(secondPageApp1);
    }

    @Test
    public void getApplicationHomePage_should_return_the_applicationPage_with_set_as_home_page_for_the_given_application() throws Exception {
        //given
        final SApplication application = repository.add(anApplication().withName("app1").withVersion("1.0").withPath("/app1").build());
        final SPage page = repository.add(aPage().withName("MyPage").withContent("The content".getBytes()).build());
        final SApplicationPage firstPage = repository.add(anApplicationPage().withName("FirstPage").withApplicationId(application.getId())
                .withPageId(page.getId()).build());
        repository.add(anApplicationPage().withName("SecondPage").withApplicationId(application.getId())
                .withPageId(page.getId()).build());

        ((SApplicationImpl) application).setHomePageId(firstPage.getId());
        repository.update((SApplicationImpl) application);

        //when
        final SApplicationPage retrievedAppPage = repository.getApplicationHomePage(application.getId());

        //then
        assertThat(retrievedAppPage).isEqualTo(firstPage);
    }

}
