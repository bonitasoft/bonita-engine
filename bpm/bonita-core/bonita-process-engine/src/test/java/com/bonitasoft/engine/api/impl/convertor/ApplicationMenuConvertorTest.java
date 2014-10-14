package com.bonitasoft.engine.api.impl.convertor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;

import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import com.bonitasoft.engine.business.application.ApplicationMenu;
import com.bonitasoft.engine.business.application.ApplicationMenuCreator;
import com.bonitasoft.engine.business.application.model.SApplicationMenu;
import com.bonitasoft.engine.business.application.model.impl.SApplicationMenuImpl;

@RunWith(MockitoJUnitRunner.class)
public class ApplicationMenuConvertorTest {


    private ApplicationMenuConvertor convertor;

    @Before
    public void setUp() throws Exception {
        convertor = new ApplicationMenuConvertor();
    }

    @Test
    public void buildSApplicationMenu_should_map_all_creator_fields() throws Exception {
        //given
        final ApplicationMenuCreator creator = new ApplicationMenuCreator(4, "main", 20, 1);
        creator.setParentId(11);

        //when
        final SApplicationMenu menu = convertor.buildSApplicationMenu(creator);

        //then
        assertThat(menu).isNotNull();
        assertThat(menu.getDisplayName()).isEqualTo("main");
        assertThat(menu.getApplicationId()).isEqualTo(4);
        assertThat(menu.getApplicationPageId()).isEqualTo(20);
        assertThat(menu.getIndex()).isEqualTo(1);
        assertThat(menu.getParentId()).isEqualTo(11);
    }

    @Test
    public void buildSApplicationMenu_should_have_null_parentId_when_not_set_on_creator() throws Exception {
        //given
        final ApplicationMenuCreator creator = new ApplicationMenuCreator(4, "main", 20, 1);

        //when
        final SApplicationMenu menu = convertor.buildSApplicationMenu(creator);

        //then
        assertThat(menu).isNotNull();
        assertThat(menu.getParentId()).isNull();
    }

    @Test
    public void toApplicationMenu_should_map_all_server_object_fields_and_set_application_id() throws Exception {
        //given
        final long appPageId = 15;
        long applicationId = 18;
        final SApplicationMenuImpl sMenu = new SApplicationMenuImpl("main", applicationId, appPageId, 1);
        sMenu.setId(3);
        sMenu.setParentId(21L);

        //when
        final ApplicationMenu menu = convertor.toApplicationMenu(sMenu);

        //then
        assertThat(menu).isNotNull();
        assertThat(menu.getId()).isEqualTo(3);
        assertThat(menu.getDisplayName()).isEqualTo("main");
        assertThat(menu.getApplicationPageId()).isEqualTo(15);
        assertThat(menu.getIndex()).isEqualTo(1);
        assertThat(menu.getParentId()).isEqualTo(21);
        assertThat(menu.getApplicationId()).isEqualTo(applicationId);
    }

    @Test
    public void toApplicationMenu_list_should_call_toApplitionMenu_for_each_element_in_the_list_and_return_the_list_of_converted_values() throws Exception {
        //given
        final SApplicationMenu sMenu1 = mock(SApplicationMenu.class);
        final SApplicationMenu sMenu2 = mock(SApplicationMenu.class);
        final ApplicationMenu menu1 = mock(ApplicationMenu.class);
        final ApplicationMenu menu2 = mock(ApplicationMenu.class);
        final ApplicationMenuConvertor convertorMock = spy(convertor);
        doReturn(menu1).when(convertorMock).toApplicationMenu(sMenu1);
        doReturn(menu2).when(convertorMock).toApplicationMenu(sMenu2);

        //when
        final List<ApplicationMenu> applicationMenus = convertorMock.toApplicationMenu(Arrays.<SApplicationMenu> asList(sMenu1, sMenu2));

        //then
        assertThat(applicationMenus).containsExactly(menu1, menu2);
    }

}
