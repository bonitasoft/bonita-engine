/**
 * Copyright (C) 2019 Bonitasoft S.A.
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
package org.bonitasoft.engine.tracking;

import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class AbstractFlushEventListenerTest extends AbstractTimeTrackerTest {

    @Test
    public void should_deactivate_notifyStopTracking() {
        final AbstractFlushEventListener listener = spy(new AbstractFlushEventListener(true, null) {

            @Override
            public FlushEventListenerResult flush(final FlushEvent flushEvent) throws Exception {
                return null;
            }

            @Override
            public void notifyStopTracking() {

            }

            @Override
            public void notifyStartTracking() {

            }
        });
        listener.deactivate();
        verify(listener, times(1)).notifyStopTracking();
    }

    @Test
    public void should_activate_notifyStartTracking() {
        final AbstractFlushEventListener listener = spy(new AbstractFlushEventListener(true, null) {

            @Override
            public FlushEventListenerResult flush(final FlushEvent flushEvent) throws Exception {
                return null;
            }

            @Override
            public void notifyStopTracking() {

            }

            @Override
            public void notifyStartTracking() {

            }
        });
        listener.activate();
        verify(listener, times(1)).notifyStartTracking();
    }
}
