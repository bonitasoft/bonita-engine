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
package org.bonitasoft.web.rest.server.datastore.applicationmenu;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.bonitasoft.engine.business.application.ApplicationMenuCreator;
import org.bonitasoft.engine.business.application.ApplicationMenuField;
import org.bonitasoft.engine.business.application.ApplicationMenuUpdater;
import org.bonitasoft.engine.business.application.impl.ApplicationMenuImpl;
import org.bonitasoft.web.rest.model.applicationmenu.ApplicationMenuItem;
import org.bonitasoft.web.rest.server.APITestWithMock;
import org.junit.Before;
import org.junit.Test;

public class ApplicationMenuItemConverterTest extends APITestWithMock {

    private static final String DISPLAY_NAME = "menu name";
    private static final long APPLICATION_ID = 1L;
    private static final long APPLICATION_PAGE_ID = 2L;
    private static final long PARENT_MENU_ID = 3L;
    private static final int INDEX = 4;

    private static final long MENU_ID = 10L;

    private ApplicationMenuItemConverter converter;

    @Before
    public void setUp() throws Exception {
        converter = new ApplicationMenuItemConverter();
    }

    @Test
    public void toApplicationMenuItem_should_map_all_fields() throws Exception {
        //given
        final ApplicationMenuImpl applicationMenu = new ApplicationMenuImpl(DISPLAY_NAME, APPLICATION_ID,
                APPLICATION_PAGE_ID, INDEX);
        applicationMenu.setParentId(PARENT_MENU_ID);
        applicationMenu.setId(MENU_ID);

        //when
        final ApplicationMenuItem item = converter.toApplicationMenuItem(applicationMenu);

        //then
        assertThat(item).isNotNull();
        assertThat(item.getId().toLong()).isEqualTo(MENU_ID);
        assertThat(item.getDisplayName()).isEqualTo(DISPLAY_NAME);
        assertThat(item.getApplicationId().toLong()).isEqualTo(APPLICATION_ID);
        assertThat(item.getApplicationPageId().toLong()).isEqualTo(APPLICATION_PAGE_ID);
        assertThat(item.getParentMenuId().toLong()).isEqualTo(PARENT_MENU_ID);
        assertThat(item.getMenuIndex()).isEqualTo(INDEX);
    }

    @Test
    public void applicationMenuItem_with_null_parent_id_should_not_return_null() throws Exception {
        //given
        final ApplicationMenuImpl applicationMenu = new ApplicationMenuImpl(DISPLAY_NAME, APPLICATION_ID,
                APPLICATION_PAGE_ID, INDEX);
        applicationMenu.setParentId(null);
        applicationMenu.setId(MENU_ID);

        //when
        final ApplicationMenuItem item = converter.toApplicationMenuItem(applicationMenu);

        //then
        assertThat(item.getAttributeValue(ApplicationMenuItem.ATTRIBUTE_PARENT_MENU_ID)).isEqualTo("-1");
    }

    @Test
    public void applicationMenuItem_with_null_page_id_should_not_return_null() throws Exception {
        //given
        final ApplicationMenuImpl applicationMenu = new ApplicationMenuImpl(DISPLAY_NAME, APPLICATION_ID, null, INDEX);
        applicationMenu.setParentId(PARENT_MENU_ID);
        applicationMenu.setId(MENU_ID);

        //when
        final ApplicationMenuItem item = converter.toApplicationMenuItem(applicationMenu);

        //then
        assertThat(item).isNotNull();
        assertThat(item.getAttributeValue(ApplicationMenuItem.ATTRIBUTE_APPLICATION_PAGE_ID)).isEqualTo("-1");
    }

    @Test
    public void toApplicationMenuCreator_should_map_all_fields() throws Exception {
        //given
        final ApplicationMenuItem item = new ApplicationMenuItem();
        item.setDisplayName(DISPLAY_NAME);
        item.setApplicationId(APPLICATION_ID);
        item.setApplicationPageId(APPLICATION_PAGE_ID);
        item.setMenuIndex(INDEX);
        item.setParentMenuId(PARENT_MENU_ID);

        //when
        final ApplicationMenuCreator creator = converter.toApplicationMenuCreator(item);

        //then
        assertThat(creator).isNotNull();
        final Map<ApplicationMenuField, Serializable> fields = creator.getFields();
        assertThat(fields.get(ApplicationMenuField.DISPLAY_NAME)).isEqualTo(DISPLAY_NAME);
        assertThat(fields.get(ApplicationMenuField.APPLICATION_ID)).isEqualTo(APPLICATION_ID);
        assertThat(fields.get(ApplicationMenuField.APPLICATION_PAGE_ID)).isEqualTo(APPLICATION_PAGE_ID);
        assertThat(fields.get(ApplicationMenuField.PARENT_ID)).isEqualTo(PARENT_MENU_ID);
    }

    @Test
    public void toApplicationMenuCreator_should_not_map_negativeId() throws Exception {
        //given
        final ApplicationMenuItem item = new ApplicationMenuItem();
        item.setDisplayName(DISPLAY_NAME);
        item.setApplicationId(APPLICATION_ID);
        item.setApplicationPageId(-1L);
        item.setMenuIndex(INDEX);
        item.setParentMenuId(-1L);

        //when
        final ApplicationMenuCreator creator = converter.toApplicationMenuCreator(item);

        //then
        assertThat(creator).isNotNull();
        final Map<ApplicationMenuField, Serializable> fields = creator.getFields();
        assertThat(fields.get(ApplicationMenuField.DISPLAY_NAME)).isEqualTo(DISPLAY_NAME);
        assertThat(fields.get(ApplicationMenuField.APPLICATION_ID)).isEqualTo(APPLICATION_ID);
        assertThat(fields.get(ApplicationMenuField.APPLICATION_PAGE_ID)).isNull();
        assertThat(fields.get(ApplicationMenuField.PARENT_ID)).isNull();
    }

    @Test
    public void toApplicationMenuUpdater_should_map_all_fields() {

        //given
        final HashMap<String, String> fields = new HashMap<>();
        fields.put(ApplicationMenuItem.ATTRIBUTE_APPLICATION_ID, String.valueOf(APPLICATION_ID));
        fields.put(ApplicationMenuItem.ATTRIBUTE_APPLICATION_PAGE_ID, String.valueOf(APPLICATION_PAGE_ID));
        fields.put(ApplicationMenuItem.ATTRIBUTE_DISPLAY_NAME, DISPLAY_NAME);
        fields.put(ApplicationMenuItem.ATTRIBUTE_MENU_INDEX, String.valueOf(INDEX));
        fields.put(ApplicationMenuItem.ATTRIBUTE_PARENT_MENU_ID, String.valueOf(PARENT_MENU_ID));

        //when
        final ApplicationMenuUpdater updater = converter.toApplicationMenuUpdater(fields);

        //then
        assertThat(updater).isNotNull();
        assertThat(updater.getFields().get(ApplicationMenuField.DISPLAY_NAME)).isEqualTo(DISPLAY_NAME);
        assertThat(updater.getFields().get(ApplicationMenuField.APPLICATION_ID)).isEqualTo(null);
        assertThat(updater.getFields().get(ApplicationMenuField.APPLICATION_PAGE_ID)).isEqualTo(APPLICATION_PAGE_ID);
        assertThat(updater.getFields().get(ApplicationMenuField.INDEX)).isEqualTo(INDEX);
        assertThat(updater.getFields().get(ApplicationMenuField.PARENT_ID)).isEqualTo(PARENT_MENU_ID);

    }

    @Test
    public void update_with_no_pageId_should_send_null() {

        //given
        final HashMap<String, String> fields = new HashMap<>();
        fields.put(ApplicationMenuItem.ATTRIBUTE_APPLICATION_ID, "-1");

        //when
        final ApplicationMenuUpdater updater = converter.toApplicationMenuUpdater(fields);

        //then
        assertThat(updater).isNotNull();
        assertThat(updater.getFields().get(ApplicationMenuField.APPLICATION_ID)).isEqualTo(null);

    }

    @Test
    public void update_with_no_parent_menu_should_send_null() {

        //given
        final HashMap<String, String> fields = new HashMap<>();
        fields.put(ApplicationMenuItem.ATTRIBUTE_APPLICATION_PAGE_ID, "-1");

        //when
        final ApplicationMenuUpdater updater = converter.toApplicationMenuUpdater(fields);

        //then
        assertThat(updater).isNotNull();
        assertThat(updater.getFields().get(ApplicationMenuField.PARENT_ID)).isEqualTo(null);
    }

}
