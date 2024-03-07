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
package org.bonitasoft.web.rest.server.datastore.converter;

import static java.util.Arrays.asList;
import static junit.framework.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

import org.bonitasoft.engine.search.SearchResult;
import org.bonitasoft.web.toolkit.client.common.exception.api.APISearchIndexOutOfRange;
import org.bonitasoft.web.toolkit.client.data.item.IItem;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

/**
 * @author Vincent Elcrin
 */
public class ItemSearchResultConverterTest {

    @Mock
    SearchResult<String> result;

    @Mock
    ItemConverter<IItem, String> converter;

    @Before
    public void initializeMocks() {
        initMocks(this);
    }

    @Test
    public void testTotalCanBeRetrieved() {
        when(result.getCount()).thenReturn(5L);

        ItemSearchResultConverter<IItem, String> itemSearchResult = new ItemSearchResultConverter<>(3, 2, result,
                converter);

        assertEquals(5L, itemSearchResult.toItemSearchResult().getTotal());
    }

    @Test
    public void testTotalSetCanBeRetrieved() {
        ItemSearchResultConverter<IItem, String> itemSearchResult = new ItemSearchResultConverter<>(3, 2, result, 8L,
                converter);

        assertEquals(8L, itemSearchResult.toItemSearchResult().getTotal());
    }

    @Test(expected = APISearchIndexOutOfRange.class)
    public void testPageOutOfResultNumberThrowsException() {
        when(result.getCount()).thenReturn(1L);

        new ItemSearchResultConverter<>(2, 10, result, converter).toItemSearchResult();
    }

    @Test
    public void testPageNumberCanBeRetrieved() {
        ItemSearchResultConverter<IItem, String> itemSearchResult = new ItemSearchResultConverter<>(5, 10, result, 8L,
                converter);

        assertEquals(5, itemSearchResult.toItemSearchResult().getPage());
    }

    @Test
    public void testResultingItemsCanBeRetrieved() {
        IItem item1 = mock(IItem.class);
        IItem item2 = mock(IItem.class);
        when(result.getResult()).thenReturn(asList("item1", "item2"));
        when(converter.convert(result.getResult())).thenReturn(asList(item1, item2));

        ItemSearchResultConverter<IItem, String> itemSearchResult = new ItemSearchResultConverter<>(1, 10, result, 2,
                converter);

        assertEquals(item1, itemSearchResult.toItemSearchResult().getResults().get(0));
        assertEquals(item2, itemSearchResult.toItemSearchResult().getResults().get(1));
    }
}
