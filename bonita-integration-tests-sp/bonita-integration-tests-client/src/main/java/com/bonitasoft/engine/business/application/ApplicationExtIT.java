/**
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 **/

package com.bonitasoft.engine.business.application;

import static org.assertj.core.api.Assertions.assertThat;

import org.bonitasoft.engine.api.ApplicationAPI;
import org.bonitasoft.engine.business.application.Application;
import org.bonitasoft.engine.page.Page;
import org.bonitasoft.engine.profile.Profile;
import org.bonitasoft.engine.search.SearchOptionsBuilder;
import org.bonitasoft.engine.search.SearchResult;
import org.bonitasoft.engine.test.annotation.Cover;
import org.bonitasoft.engine.test.annotation.Cover.BPMNConcept;
import org.junit.Test;

/**
 * @author Elias Ricken de Medeiros
 */
public class ApplicationExtIT extends org.bonitasoft.engine.business.application.TestWithApplication {

    @Cover(classes = { ApplicationAPI.class }, concept = BPMNConcept.APPLICATION, jira = "BS-13007", keywords = { "Application", "create", "layout" })
    @Test
    public void createApplication_with_specific_layout_returns_application_with_given_layout() throws Exception {
        //given
        final Profile profile = getProfileUser();
        Page layout = createPage("custompage_customizedLayout");
        ApplicationCreatorExt creator = new ApplicationCreatorExt("My-Application", "My application display name", "1.0", layout.getId());
        creator.setDescription("This is my application");
        creator.setIconPath("/icon.jpg");
        creator.setProfileId(profile.getId());

        //when
        final org.bonitasoft.engine.business.application.Application application = getApplicationAPI().createApplication(creator);

        //then
        assertThat(application).isNotNull();
        assertThat(application.getToken()).isEqualTo("My-Application");
        assertThat(application.getDisplayName()).isEqualTo("My application display name");
        assertThat(application.getVersion()).isEqualTo("1.0");
        assertThat(application.getId()).isGreaterThan(0);
        assertThat(application.getDescription()).isEqualTo("This is my application");
        assertThat(application.getIconPath()).isEqualTo("/icon.jpg");
        assertThat(application.getCreatedBy()).isEqualTo(getUser().getId());
        assertThat(application.getUpdatedBy()).isEqualTo(getUser().getId());
        assertThat(application.getHomePageId()).isNull();
        assertThat(application.getProfileId()).isEqualTo(profile.getId());
        assertThat(application.getLayoutId()).isEqualTo(layout.getId());

        getApplicationAPI().deleteApplication(application.getId());
        getPageAPI().deletePage(layout.getId());
    }

    @Cover(classes = { ApplicationAPI.class }, concept = BPMNConcept.APPLICATION, jira = "BS-13007", keywords = { "Application", "create", "default layout" })
    @Test
    public void createApplication_without_specific_layout_returns_application_with_given_default_layout() throws Exception {
        //given
        Page layout = getPageAPI().getPageByName("custompage_layout");
        ApplicationCreatorExt creator = new ApplicationCreatorExt("My-Application", "My application display name", "1.0");

        //when
        final org.bonitasoft.engine.business.application.Application application = getApplicationAPI().createApplication(creator);

        //then
        assertThat(application).isNotNull();
        assertThat(application.getLayoutId()).isEqualTo(layout.getId());

        getApplicationAPI().deleteApplication(application.getId());
    }

    @Cover(classes = { ApplicationAPI.class }, concept = BPMNConcept.APPLICATION, jira = "BS-13007", keywords = { "Application", "update", "layout" })
    @Test
    public void updateApplication_layout_should_return_application_up_with_new_layout() throws Exception {
        //given
        Page layout = createPage("custompage_customizedLayout");
        final ApplicationCreatorExt creator = new ApplicationCreatorExt("My-Application", "My application display name", "1.0");
        final org.bonitasoft.engine.business.application.Application application = getApplicationAPI().createApplication(creator);

        final ApplicationUpdaterExt updater = new ApplicationUpdaterExt();
        updater.setToken("My-updated-app");
        updater.setLayoutId(layout.getId());

        //when
        final org.bonitasoft.engine.business.application.Application updatedApplication = getApplicationAPI().updateApplication(application.getId(), updater);

        //then
        assertThat(updatedApplication).isNotNull();
        assertThat(updatedApplication.getToken()).isEqualTo("My-updated-app");
        assertThat(updatedApplication.getLayoutId()).isEqualTo(layout.getId());
        assertThat(updatedApplication).isEqualTo(getApplicationAPI().getApplication(application.getId()));

        getApplicationAPI().deleteApplication(application.getId());
        getPageAPI().deletePage(layout.getId());
    }

    @Cover(classes = { ApplicationAPI.class }, concept = BPMNConcept.APPLICATION, jira = "BS-13007", keywords = { "Application", "search",
            "filter on layoutId", "no search term" })
    @Test
    public void searchApplications_can_filter_on_layoutId() throws Exception {
        //given
        Page layout = createPage("custompage_newLayout");
        final ApplicationCreatorExt hrCreator = new ApplicationCreatorExt("HR-dashboard", "HR dash board", "1.0");
        final ApplicationCreatorExt engineeringCreator = new ApplicationCreatorExt("Engineering-dashboard", "Engineering dashboard", "1.0", layout.getId());
        final ApplicationCreatorExt marketingCreator = new ApplicationCreatorExt("Marketing-dashboard", "Marketing dashboard", "1.0");

        final org.bonitasoft.engine.business.application.Application hr = getApplicationAPI().createApplication(hrCreator);
        final org.bonitasoft.engine.business.application.Application engineering = getApplicationAPI().createApplication(engineeringCreator);
        final org.bonitasoft.engine.business.application.Application marketing = getApplicationAPI().createApplication(marketingCreator);

        //when
        final SearchOptionsBuilder builder = getAppSearchBuilderOrderByToken(0, 10);
        builder.filter(org.bonitasoft.engine.business.application.ApplicationSearchDescriptor.LAYOUT_ID, layout.getId());

        final SearchResult<Application> applications = getApplicationAPI().searchApplications(builder.done());
        assertThat(applications).isNotNull();
        assertThat(applications.getCount()).isEqualTo(1);
        assertThat(applications.getResult()).containsExactly(engineering);

        //clean
        getApplicationAPI().deleteApplication(hr.getId());
        getApplicationAPI().deleteApplication(engineering.getId());
        getApplicationAPI().deleteApplication(marketing.getId());

        getPageAPI().deletePage(layout.getId());
    }

}
