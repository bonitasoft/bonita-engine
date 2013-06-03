/**
 * Copyright (C) 2013 BonitaSoft S.A.
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
package org.bonitasoft.engine.identity.impl.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;

import org.bonitasoft.engine.events.EventService;
import org.bonitasoft.engine.identity.SGroupNotFoundException;
import org.bonitasoft.engine.identity.SIdentityException;
import org.bonitasoft.engine.identity.impl.IdentityServiceImpl;
import org.bonitasoft.engine.identity.model.SGroup;
import org.bonitasoft.engine.identity.recorder.SelectDescriptorBuilder;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.persistence.OrderByType;
import org.bonitasoft.engine.persistence.QueryOptions;
import org.bonitasoft.engine.persistence.ReadPersistenceService;
import org.bonitasoft.engine.persistence.SBonitaReadException;
import org.bonitasoft.engine.persistence.SBonitaSearchException;
import org.bonitasoft.engine.persistence.SelectOneDescriptor;
import org.bonitasoft.engine.recorder.Recorder;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.internal.stubbing.answers.Returns;

/**
 * @author Matthieu Chaffotte
 * @author Celine Souchet
 */
public class IdentityServiceImplForGroupTest {

    Recorder recorder;

    ReadPersistenceService persistence;

    EventService eventService;

    TechnicalLoggerService logger;

    private IdentityServiceImpl identityServiceImpl;

    @Before
    public void setup() {
        recorder = mock(Recorder.class);
        persistence = mock(ReadPersistenceService.class);
        eventService = mock(EventService.class);
        logger = mock(TechnicalLoggerService.class, new Returns(true));
        identityServiceImpl = new IdentityServiceImpl(persistence, recorder, eventService, null, logger, null, null);
    }

    @Test
    public void getNumberOfGroupChildren() throws Exception {

        final SGroup group = mock(SGroup.class);
        when(group.getParentPath()).thenReturn("/thePath");
        when(persistence.selectById(SelectDescriptorBuilder.getElementById(SGroup.class, "Group", 123l))).thenReturn(group);
        when(persistence.selectOne(SelectDescriptorBuilder.getNumberOfGroupChildren("/thePath"))).thenReturn(12l);

        assertEquals(12l, identityServiceImpl.getNumberOfGroupChildren(123l));
    }

    @Test(expected = SIdentityException.class)
    public void getNumberOfGroupChildrenThrowExceptions() throws Exception {

        final SGroup group = mock(SGroup.class);
        when(group.getParentPath()).thenReturn("/thePath");
        when(persistence.selectById(SelectDescriptorBuilder.getElementById(SGroup.class, "Group", 123l))).thenReturn(group);
        when(persistence.selectOne(SelectDescriptorBuilder.getNumberOfGroupChildren("/thePath"))).thenThrow(new SBonitaReadException(""));

        identityServiceImpl.getNumberOfGroupChildren(123l);
    }

    @Test
    public void getGroupChildren() throws Exception {

        final SGroup group = mock(SGroup.class);
        final SGroup child = mock(SGroup.class);
        when(persistence.selectById(SelectDescriptorBuilder.getElementById(SGroup.class, "Group", 123l))).thenReturn(group);
        when(persistence.selectList(SelectDescriptorBuilder.getChildrenOfGroup(group))).thenReturn(Collections.singletonList(child));

        assertEquals(child, identityServiceImpl.getGroupChildren(123l).get(0));
    }

    @Test(expected = SIdentityException.class)
    public void getGroupChildrenThrowExceptions() throws Exception {

        final SGroup group = mock(SGroup.class);
        when(persistence.selectById(SelectDescriptorBuilder.getElementById(SGroup.class, "Group", 123l))).thenReturn(group);
        when(persistence.selectList(SelectDescriptorBuilder.getChildrenOfGroup(group))).thenThrow(new SBonitaReadException(""));

        identityServiceImpl.getGroupChildren(123l);
    }

    @Test
    public void getGroupChildrenPaginated() throws Exception {

        final SGroup group = mock(SGroup.class);
        final SGroup child = mock(SGroup.class);
        when(persistence.selectById(SelectDescriptorBuilder.getElementById(SGroup.class, "Group", 123l))).thenReturn(group);
        when(persistence.selectList(SelectDescriptorBuilder.getChildrenOfGroup(group, 0, 10))).thenReturn(Collections.singletonList(child));

        assertEquals(child, identityServiceImpl.getGroupChildren(123l, 0, 10).get(0));
    }

    @Test(expected = SIdentityException.class)
    public void getGroupChildrenPaginatedThrowExceptions() throws Exception {

        final SGroup group = mock(SGroup.class);
        when(persistence.selectById(SelectDescriptorBuilder.getElementById(SGroup.class, "Group", 123l))).thenReturn(group);
        when(persistence.selectList(SelectDescriptorBuilder.getChildrenOfGroup(group, 0, 10))).thenThrow(new SBonitaReadException(""));

        identityServiceImpl.getGroupChildren(123l, 0, 10);
    }

    @Test
    public void getGroupChildrenPaginatedOrder() throws Exception {

        final SGroup group = mock(SGroup.class);
        final SGroup child = mock(SGroup.class);
        when(persistence.selectById(SelectDescriptorBuilder.getElementById(SGroup.class, "Group", 123l))).thenReturn(group);
        when(persistence.selectList(SelectDescriptorBuilder.getChildrenOfGroup(group, "name", OrderByType.ASC, 0, 10))).thenReturn(
                Collections.singletonList(child));

        assertEquals(child, identityServiceImpl.getGroupChildren(123l, 0, 10, "name", OrderByType.ASC).get(0));
    }

    @Test(expected = SIdentityException.class)
    public void getGroupChildrenPaginatedOrderThrowExceptions() throws Exception {

        final SGroup group = mock(SGroup.class);
        when(persistence.selectById(SelectDescriptorBuilder.getElementById(SGroup.class, "Group", 123l))).thenReturn(group);
        when(persistence.selectList(SelectDescriptorBuilder.getChildrenOfGroup(group, "name", OrderByType.ASC, 0, 10))).thenThrow(new SBonitaReadException(""));

        identityServiceImpl.getGroupChildren(123l, 0, 10, "name", OrderByType.ASC);
    }

    @Test
    public void getNumberOfGroupsWithOptions() throws Exception {

        final QueryOptions options = new QueryOptions(0, 10);
        when(persistence.getNumberOfEntities(SGroup.class, options, null)).thenReturn(125l);

        assertEquals(125, identityServiceImpl.getNumberOfGroups(options));
    }

    @Test(expected = SBonitaSearchException.class)
    public void getNumberOfGroupsWithOptionsThrowExceptions() throws Exception {

        final QueryOptions options = new QueryOptions(0, 10);
        when(persistence.getNumberOfEntities(SGroup.class, options, null)).thenThrow(new SBonitaReadException(""));

        identityServiceImpl.getNumberOfGroups(options);
    }

    @Test
    public void getNumberOfGroups() throws Exception {
        when(persistence.selectOne(SelectDescriptorBuilder.getNumberOfElement("SGroup", SGroup.class))).thenReturn(125l);

        assertEquals(125, identityServiceImpl.getNumberOfGroups());
    }

    @Test(expected = SIdentityException.class)
    public void getNumberOfGroupsThrowExceptions() throws Exception {
        when(persistence.selectOne(SelectDescriptorBuilder.getNumberOfElement("SGroup", SGroup.class))).thenThrow(new SBonitaReadException(""));

        identityServiceImpl.getNumberOfGroups();
    }

    @Test
    public void searchGroups() throws Exception {
        final QueryOptions options = new QueryOptions(0, 10);
        final SGroup group = mock(SGroup.class);
        when(persistence.searchEntity(SGroup.class, options, null)).thenReturn(Collections.singletonList(group));

        assertEquals(group, identityServiceImpl.searchGroups(options).get(0));
    }

    @Test(expected = SBonitaSearchException.class)
    public void searchGroupsThrowExceptions() throws Exception {
        final QueryOptions options = new QueryOptions(0, 10);
        when(persistence.searchEntity(SGroup.class, options, null)).thenThrow(new SBonitaReadException(""));

        identityServiceImpl.searchGroups(options);
    }

    @Test
    public void getGroup() throws Exception {
        final SGroup group = mock(SGroup.class);
        when(persistence.selectById(SelectDescriptorBuilder.getElementById(SGroup.class, "Group", 123l))).thenReturn(group);

        assertEquals(group, identityServiceImpl.getGroup(123l));
    }

    @Test(expected = SIdentityException.class)
    public void getGroupThrowExceptions() throws Exception {
        when(persistence.selectById(SelectDescriptorBuilder.getElementById(SGroup.class, "Group", 123l))).thenThrow(new SBonitaReadException(""));

        identityServiceImpl.getGroup(123l);
    }

    @Test(expected = SGroupNotFoundException.class)
    public void getGroupNotFound() throws Exception {
        final SGroup group = mock(SGroup.class);

        assertEquals(group, identityServiceImpl.getGroup(123l));
    }

    @Test
    public void getGroupsByName() throws Exception {
        final SGroup group = mock(SGroup.class);
        when(persistence.selectList(SelectDescriptorBuilder.getGroupsByName("name"))).thenReturn(Collections.singletonList(group));

        assertEquals(group, identityServiceImpl.getGroupsByName("name").iterator().next());
    }

    @Test(expected = SIdentityException.class)
    public void getGroupByNameThrowExceptions() throws Exception {
        when(persistence.selectList(SelectDescriptorBuilder.getGroupsByName("name"))).thenThrow(new SBonitaReadException(""));

        identityServiceImpl.getGroupsByName("name");
    }

    @Test
    public void getGroupByPath() throws Exception {
        final SGroup group = mock(SGroup.class);
        when(persistence.selectOne(SelectDescriptorBuilder.getGroupByName("path"))).thenReturn(group);

        assertEquals(group, identityServiceImpl.getGroupByPath("/path"));
    }

    @Test(expected = SGroupNotFoundException.class)
    public void getGroupByPathDoesNotExists() throws Exception {
        final SGroup group = mock(SGroup.class);

        assertEquals(group, identityServiceImpl.getGroupByPath("/path"));
    }

    @Test
    public void getGroupByPathWithNoSlash() throws Exception {
        final SGroup group = mock(SGroup.class);
        when(persistence.selectOne(SelectDescriptorBuilder.getGroupByName("path"))).thenReturn(group);

        assertEquals(group, identityServiceImpl.getGroupByPath("path"));
    }

    @Test
    public void getGroupByPathThatIsNotRoot() throws Exception {
        final SGroup group = mock(SGroup.class);
        when(persistence.selectOne(SelectDescriptorBuilder.getGroupByPath("/path", "subPath"))).thenReturn(group);

        assertEquals(group, identityServiceImpl.getGroupByPath("/path/subPath"));
    }

    @Test(expected = SIdentityException.class)
    public void getGroupByPathThrowExceptions() throws Exception {
        when(persistence.selectOne(Matchers.<SelectOneDescriptor<SGroup>> any())).thenThrow(new SBonitaReadException(""));

        identityServiceImpl.getGroupByPath("path");
    }

    @Test
    public void getGroups() throws Exception {
        final SGroup group = mock(SGroup.class);
        when(persistence.selectList(SelectDescriptorBuilder.getElementsByIds(SGroup.class, "Group", Arrays.asList(123l)))).thenReturn(Arrays.asList(group));

        assertEquals(group, identityServiceImpl.getGroups(Arrays.asList(123l)).get(0));
    }

    @Test
    public void getGroupsNullIds() throws Exception {
        assertTrue(identityServiceImpl.getGroups(null).isEmpty());
    }

    @Test
    public void getGroupsEmptyIds() throws Exception {
        assertTrue(identityServiceImpl.getGroups(Collections.<Long> emptyList()).isEmpty());
    }

    @Test(expected = SIdentityException.class)
    public void getGroupsThrowExceptions() throws Exception {
        when(persistence.selectList(SelectDescriptorBuilder.getElementsByIds(SGroup.class, "Group", Arrays.asList(123l)))).thenThrow(
                new SBonitaReadException(""));

        identityServiceImpl.getGroups(Arrays.asList(123l));
    }

    @Test
    public void getGroupsList() throws Exception {
        final SGroup group = mock(SGroup.class);
        when(persistence.selectList(SelectDescriptorBuilder.getElements(SGroup.class, "Group", 0, 10))).thenReturn(Arrays.asList(group));

        assertEquals(group, identityServiceImpl.getGroups(0, 10).get(0));
    }

    @Test(expected = SIdentityException.class)
    public void getGroupsListThrowExceptions() throws Exception {
        when(persistence.selectList(SelectDescriptorBuilder.getElements(SGroup.class, "Group", 0, 10))).thenThrow(new SBonitaReadException(""));

        identityServiceImpl.getGroups(0, 10);
    }

    @Test
    public void getGroupsListOrdered() throws Exception {
        final SGroup group = mock(SGroup.class);
        when(persistence.selectList(SelectDescriptorBuilder.getElements(SGroup.class, "Group", "name", OrderByType.ASC, 0, 10))).thenReturn(
                Arrays.asList(group));

        assertEquals(group, identityServiceImpl.getGroups(0, 10, "name", OrderByType.ASC).get(0));
    }

    @Test(expected = SIdentityException.class)
    public void getGroupsListOrderedThrowExceptions() throws Exception {
        when(persistence.selectList(SelectDescriptorBuilder.getElements(SGroup.class, "Group", "name", OrderByType.ASC, 0, 10))).thenThrow(
                new SBonitaReadException(""));

        identityServiceImpl.getGroups(0, 10, "name", OrderByType.ASC);
    }

}
