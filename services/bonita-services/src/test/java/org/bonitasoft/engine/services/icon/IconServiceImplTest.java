/**
 * Copyright (C) 2021 Bonitasoft S.A.
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
package org.bonitasoft.engine.services.icon;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import org.bonitasoft.engine.persistence.SelectByIdDescriptor;
import org.bonitasoft.engine.recorder.Recorder;
import org.bonitasoft.engine.recorder.model.InsertRecord;
import org.bonitasoft.engine.recorder.model.Record;
import org.bonitasoft.engine.services.PersistenceService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class IconServiceImplTest {

    @Mock
    Recorder recorder;
    @Mock
    PersistenceService persistenceService;
    @Captor
    ArgumentCaptor<? extends Record> record;
    @InjectMocks
    IconServiceImpl iconService;

    @Test
    void should_create_icon_with_specific_mimetype() throws Exception {
        iconService.createIcon("myAvatar12.png", new byte[] { 1, 2, 3 });

        //the file name is not kept, do not verify it
        verify(recorder).recordInsert(((InsertRecord) record.capture()), eq("ICON"));
        assertThat(((SIcon) record.getValue().getEntity())).satisfies(
                entity -> {
                    assertThat(entity.getMimeType()).isEqualTo("image/png");
                    assertThat(entity.getContent()).isEqualTo(new byte[] { 1, 2, 3 });
                });
    }

    @Test
    void should_delete_icon_when_it_exists() throws Exception {
        doReturn(new SIcon(1L, 42L, "image/jpeg", new byte[] { 1, 2, 3 })).when(persistenceService)
                .selectById(new SelectByIdDescriptor<>(SIcon.class, 42L));

        iconService.deleteIcon(42L);

        verify(recorder).recordDelete(argThat(r -> r.getEntity().getId() == 42L), eq("ICON"));
    }

    @Test
    void should_not_delete_icon_when_it_does_not_exists() throws Exception {
        iconService.deleteIcon(42L);

        verifyNoInteractions(recorder);
    }

}
