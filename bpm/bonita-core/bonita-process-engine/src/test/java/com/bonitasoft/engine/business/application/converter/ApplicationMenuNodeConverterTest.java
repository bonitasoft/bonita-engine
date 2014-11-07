/*******************************************************************************
 * Copyright (C) 2014 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 ******************************************************************************/
package com.bonitasoft.engine.business.application.converter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.bonitasoft.engine.persistence.QueryOptions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.bonitasoft.engine.business.application.ApplicationService;
import com.bonitasoft.engine.business.application.model.SApplicationMenu;
import com.bonitasoft.engine.business.application.model.impl.SApplicationMenuImpl;
import com.bonitasoft.engine.business.application.model.impl.SApplicationPageImpl;
import com.bonitasoft.engine.business.application.xml.ApplicationMenuNode;
import com.bonitasoft.engine.business.application.xml.ApplicationNode;

@RunWith(MockitoJUnitRunner.class)
public class ApplicationMenuNodeConverterTest {

    @Mock
    ApplicationService applicationService;

    @InjectMocks
    private ApplicationMenuNodeConverter converter;

    @Test(expected = IllegalArgumentException.class)
    public void convertNullMenuShouldThrowIllegalArgument() throws Exception {
        converter.toMenu(null);
    }

    @Test
    public void convertMenuShouldConvertAllFields() throws Exception {
        // given:
        final Long applicationPageId = 888L;
        final String displayName = "Readable menu name for display";
        final String token = "application-page-token";
        doReturn(new SApplicationPageImpl(91L, applicationPageId, token)).when(applicationService).getApplicationPage(applicationPageId);

        // when:
        final ApplicationMenuNode convertedMenu = converter.toMenu(new SApplicationMenuImpl(displayName, 147L, applicationPageId, 14));

        // then:
        assertThat(convertedMenu.getApplicationPage()).isEqualTo(token);
        assertThat(convertedMenu.getDisplayName()).isEqualTo(displayName);
    }

    @Test
    public void addMenusToApplicationNodeShouldBeRecursive() throws Exception {
        // given:
        final long applicationId = 333L;
        final Long parentMenuId = null;
        final Long applicationPageId1 = 44L;

        final String displayName1 = "HR";
        final SApplicationMenu sApplicationMenu1 = new SApplicationMenuImpl(displayName1, applicationId, applicationPageId1, 1);
        final List<SApplicationMenu> level1Menus = new ArrayList<SApplicationMenu>();
        level1Menus.add(sApplicationMenu1);

        final List<SApplicationMenu> level2Menus = new ArrayList<SApplicationMenu>();
        final String displayName11 = "Legal HR procedures";
        final SApplicationMenu sApplicationMenu11 = new SApplicationMenuImpl(displayName11, applicationId, null, 1);
        level2Menus.add(sApplicationMenu11);
        final String displayName12 = "HR collective agreement";
        final Long applicationPageId12 = 577L;
        final SApplicationMenu sApplicationMenu12 = new SApplicationMenuImpl(displayName12, applicationId, applicationPageId12, 2);
        level2Menus.add(sApplicationMenu12);

        given(applicationService.searchApplicationMenus(any(QueryOptions.class))).willReturn(level1Menus).willReturn(level2Menus)
                .willReturn(Collections.<SApplicationMenu> emptyList());

        final String token1 = "mytoken-level-1";
        given(applicationService.getApplicationPage(applicationPageId1)).willReturn(new SApplicationPageImpl(applicationId, applicationPageId1, token1));
        final String token12 = "mytoken-level-12";
        given(applicationService.getApplicationPage(applicationPageId12)).willReturn(new SApplicationPageImpl(applicationId, applicationPageId12, token12));

        // when:
        final ApplicationNode node = new ApplicationNode();
        converter.addMenusToApplicationNode(applicationId, parentMenuId, node, null);

        // then:
        assertThat(node.getApplicationMenus().size()).isEqualTo(1);
        final ApplicationMenuNode menuNode1 = node.getApplicationMenus().get(0);
        assertThat(menuNode1.getApplicationPage()).isEqualTo(token1);
        assertThat(menuNode1.getDisplayName()).isEqualTo(displayName1);
        assertThat(menuNode1.getApplicationMenus().size()).isEqualTo(2);
        final ApplicationMenuNode menuNode11 = menuNode1.getApplicationMenus().get(0);
        assertThat(menuNode11.getApplicationPage()).isNull(); // no page linked to that menu
        assertThat(menuNode11.getDisplayName()).isEqualTo(displayName11);
        final ApplicationMenuNode menuNode12 = menuNode1.getApplicationMenus().get(1);
        assertThat(menuNode12.getApplicationPage()).isEqualTo(token12);
        assertThat(menuNode12.getDisplayName()).isEqualTo(displayName12);
    }

}
