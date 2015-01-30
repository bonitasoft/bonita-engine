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
package org.bonitasoft.engine.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.bonitasoft.engine.test.persistence.builder.ApplicationBuilder.anApplication;
import static org.bonitasoft.engine.test.persistence.builder.ApplicationMenuBuilder.anApplicationMenu;
import static org.bonitasoft.engine.test.persistence.builder.ApplicationPageBuilder.anApplicationPage;
import static org.bonitasoft.engine.test.persistence.builder.PageBuilder.aPage;

import javax.inject.Inject;

import org.bonitasoft.engine.business.application.model.SApplication;
import org.bonitasoft.engine.business.application.model.SApplicationMenu;
import org.bonitasoft.engine.business.application.model.SApplicationPage;
import org.bonitasoft.engine.business.application.model.impl.SApplicationImpl;
import org.bonitasoft.engine.page.SPage;
import org.bonitasoft.engine.test.persistence.repository.ApplicationRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Elias Ricken de Medeiros
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "/testContext.xml" })
@Transactional
public class ApplicationQueriesTest {

    @Inject
    private ApplicationRepository repository;

    @Test
    public void getApplicationByToken_returns_the_application_with_the_given_token() throws Exception {
        //given
        repository.add(anApplication().withToken("app1").withDispalyName("my app1").withDispalyName("my app1").withVersion("1.0").withPath("app1").build());
        final SApplication application2 = repository.add(anApplication().withToken("app2").withDispalyName("my app2").withDispalyName("my app2")
                .withDispalyName("my app2").withVersion("1.0").withPath("/app2")
                .build());
        repository.add(anApplication().withToken("app3").withDispalyName("my app3").withDispalyName("my app3").withVersion("1.0").withPath("app3").build());

        //when
        final SApplication retrievedApp = repository.getApplicationByToken("app2");

        //then
        assertThat(retrievedApp).isEqualTo(application2);
    }

    @Test
    public void getApplication_returns_the_application_with_the_given_id() throws Exception {
        //given
        repository.add(anApplication().withToken("app1").withDispalyName("my app1").withDispalyName("my app1").withVersion("1.0").withPath("app1").build());
        final SApplication application2 = repository.add(anApplication().withToken("app2").withDispalyName("my app2").withDispalyName("my app2")
                .withVersion("1.0").withPath("app1").build());
        repository.add(anApplication().withToken("app3").withDispalyName("my app3").withDispalyName("my app3").withVersion("1.0").withPath("app1").build());

        //when
        final SApplication retrievedApp = repository.getApplication(application2.getId());

        //then
        assertThat(retrievedApp).isEqualTo(application2);
    }

    @Test
    public void getApplicationPageById_should_return_the_applicationPage_identified_by_the_given_id() throws Exception {
        //given
        final SApplication application1 = repository.add(anApplication().withToken("app1").withDispalyName("my app1").withDispalyName("my app")
                .withVersion("1.0").withPath("/app1").build());
        final SPage page = repository.add(aPage().withName("MyPage").withContent("The content".getBytes()).build());
        repository.add(anApplicationPage().withToken("FirstPage").withApplicationId(application1.getId()).withPageId(page.getId()).build());
        final SApplicationPage secondPageApp = repository.add(anApplicationPage().withToken("SecondPage").withApplicationId(application1.getId())
                .withPageId(page.getId()).build());
        repository.add(anApplicationPage().withToken("ThirdPage").withApplicationId(application1.getId()).withPageId(page.getId()).build());

        //when
        final SApplicationPage retrievedAppPage = repository.getApplicationPage(secondPageApp.getId());

        //then
        assertThat(retrievedAppPage).isEqualTo(secondPageApp);
    }

    @Test
    public void getApplicationPageByNameAnApplicationName_should_return_the_applicationPage_with_the_given_name_in_the_given_application() throws Exception {
        //given
        final SApplication application1 = repository.add(anApplication().withToken("app1").withDispalyName("my app1").withDispalyName("my app")
                .withVersion("1.0").withPath("/app1")
                .build());
        final SApplication application2 = repository.add(anApplication().withToken("app2").withDispalyName("my app2").withDispalyName("my app")
                .withVersion("1.0").withPath("/app2")
                .build());
        final SPage page = repository.add(aPage().withName("MyPage").withContent("The content".getBytes()).build());
        repository.add(anApplicationPage().withToken("FirstPage").withApplicationId(application1.getId()).withPageId(page.getId()).build());
        final SApplicationPage secondPageApp1 = repository.add(anApplicationPage().withToken("SecondPage").withApplicationId(application1.getId())
                .withPageId(page.getId()).build());
        repository.add(anApplicationPage().withToken("FirstPage").withApplicationId(application2.getId()).withPageId(page.getId()).build());
        repository.add(anApplicationPage().withToken("SecondPage").withApplicationId(application2.getId()).withPageId(page.getId()).build());

        //when
        final SApplicationPage retrievedAppPage = repository.getApplicationPageByTokenAndApplicationToken("app1", "SecondPage");

        //then
        assertThat(retrievedAppPage).isEqualTo(secondPageApp1);
    }

    @Test
    public void getApplicationPageByTokenAndApplicationId_should_return_the_applicationPage_with_the_given_name_in_the_given_application() throws Exception {
        //given
        final SApplication application1 = repository.add(anApplication().withToken("app1").withDispalyName("my app1").withDispalyName("my app")
                .withVersion("1.0").withPath("/app1")
                .build());
        final SApplication application2 = repository.add(anApplication().withToken("app2").withDispalyName("my app2").withDispalyName("my app")
                .withVersion("1.0").withPath("/app2")
                .build());
        final SPage page = repository.add(aPage().withName("MyPage").withContent("The content".getBytes()).build());
        repository.add(anApplicationPage().withToken("FirstPage").withApplicationId(application1.getId()).withPageId(page.getId()).build());
        final SApplicationPage secondPageApp1 = repository.add(anApplicationPage().withToken("SecondPage").withApplicationId(application1.getId())
                .withPageId(page.getId()).build());
        repository.add(anApplicationPage().withToken("FirstPage").withApplicationId(application2.getId()).withPageId(page.getId()).build());
        repository.add(anApplicationPage().withToken("SecondPage").withApplicationId(application2.getId()).withPageId(page.getId()).build());

        //when
        final SApplicationPage retrievedAppPage = repository.getApplicationPageByTokenAndApplicationId(application1.getId(), "SecondPage");

        //then
        assertThat(retrievedAppPage).isEqualTo(secondPageApp1);
    }

    @Test
    public void getApplicationHomePage_should_return_the_applicationPage_set_as_home_page_for_the_given_application() throws Exception {
        //given
        final SApplication application = repository.add(anApplication().withToken("app1").withDispalyName("my app1").withDispalyName("my app")
                .withVersion("1.0").withPath("/app1")
                .build());
        final SPage page = repository.add(aPage().withName("MyPage").withContent("The content".getBytes()).build());
        final SApplicationPage firstPage = repository.add(anApplicationPage().withToken("FirstPage").withApplicationId(application.getId())
                .withPageId(page.getId()).build());
        repository.add(anApplicationPage().withToken("SecondPage").withApplicationId(application.getId())
                .withPageId(page.getId()).build());

        ((SApplicationImpl) application).setHomePageId(firstPage.getId());
        repository.update((SApplicationImpl) application);

        //when
        final SApplicationPage retrievedAppPage = repository.getApplicationHomePage(application.getId());

        //then
        assertThat(retrievedAppPage).isEqualTo(firstPage);
    }

    @Test
    public void getApplicationMenu_by_id_should_return_the_application_menu_identified_by_the_given_id() throws Exception {
        //given
        final SApplication application = repository.add(anApplication().withToken("app1").withDispalyName("my app1").withDispalyName("my app")
                .withVersion("1.0").withPath("/app1")
                .build());
        final SPage page = repository.add(aPage().withName("MyPage").withContent("The content".getBytes()).build());
        final SApplicationPage appPage = repository.add(anApplicationPage().withToken("FirstPage").withApplicationId(application.getId())
                .withPageId(page.getId()).build());
        final SApplicationMenu menu = repository.add(anApplicationMenu().withApplicationId(application.getId()).withApplicationPageId(appPage.getId())
                .withDisplayName("menu app1").withIndex(1)
                .build());

        //when
        final SApplicationMenu retrievedMenu = repository.getApplicationMenu(menu.getId());

        //then
        assertThat(retrievedMenu).isEqualTo(retrievedMenu);
    }

    @Test
    public void getLastIndexForRootMenu_should_return_last_used_index() throws Exception {
        //given
        final SApplication application = repository.add(anApplication().withToken("app1").withDispalyName("my app1").withDispalyName("my app")
                .withVersion("1.0").withPath("/app1")
                .build());
        final SPage page = repository.add(aPage().withName("MyPage").withContent("The content".getBytes()).build());
        final SApplicationPage appPage = repository.add(anApplicationPage().withToken("FirstPage").withApplicationId(application.getId())
                .withPageId(page.getId()).build());
        repository.add(anApplicationMenu().withApplicationId(application.getId()).withApplicationPageId(appPage.getId()).withDisplayName("menu app1")
                .withIndex(1)
                .build());

        //when
        final int lastIndex = repository.getLastIndexForRootMenu();

        //then
        assertThat(lastIndex).isEqualTo(1);
    }

    @Test
    public void getLastIndexForChildMenu_should_return_last_used_index_by_children_of_a_given_parent() throws Exception {
        //given
        final SApplication application = repository.add(anApplication().withToken("app1").withDispalyName("my app1").withDispalyName("my app")
                .withVersion("1.0").withPath("/app1")
                .build());
        final SPage page = repository.add(aPage().withName("MyPage").withContent("The content".getBytes()).build());
        final SApplicationPage appPage = repository.add(anApplicationPage().withToken("FirstPage").withApplicationId(application.getId())
                .withPageId(page.getId()).build());
        final SApplicationMenu parentMenu = repository.add(anApplicationMenu().withApplicationId(application.getId()).withApplicationPageId(appPage.getId())
                .withDisplayName("menu app1").withIndex(1)
                .build());
        repository.add(anApplicationMenu().withApplicationId(application.getId()).withParentId(parentMenu.getId()).withApplicationPageId(appPage.getId())
                .withDisplayName("menu app1").withIndex(1)
                .build());

        //when
        final int lastIndex = repository.getLastIndexForChildOf(parentMenu.getId());

        //then
        assertThat(lastIndex).isEqualTo(1);
    }

}
