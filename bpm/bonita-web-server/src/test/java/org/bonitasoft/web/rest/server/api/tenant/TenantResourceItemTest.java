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
package org.bonitasoft.web.rest.server.api.tenant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import org.bonitasoft.engine.tenant.TenantResource;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class TenantResourceItemTest {

    @Mock
    private TenantResource tenantResource;

    @Test
    public void should_format_lastUpdateDate_as_ISO8601_string_when_tenantResourceItem_is_created() {
        String dateAsString = "2018-01-05T09:04:19Z";
        Instant instant = Instant.ofEpochSecond(1515143059);
        when(tenantResource.getLastUpdateDate()).thenReturn(OffsetDateTime.ofInstant(instant, ZoneOffset.UTC));

        TenantResourceItem tenantResourceItem = new TenantResourceItem(tenantResource);

        assertThat(tenantResourceItem.getLastUpdateDate()).isEqualTo(dateAsString);
    }
}
