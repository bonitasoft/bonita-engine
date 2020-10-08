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
package org.bonitasoft.engine.platform.configuration;

import java.util.List;

import org.bonitasoft.engine.commons.CollectionUtil;
import org.bonitasoft.engine.commons.PlatformRestartHandler;
import org.springframework.boot.autoconfigure.condition.ConditionalOnSingleCandidate;
import org.springframework.stereotype.Component;

/**
 * @author Baptiste Mesta
 * @author Matthieu Chaffotte
 * @author Laurent Vaills
 * @author Celine Souchet
 */
@Component
@ConditionalOnSingleCandidate(NodeConfiguration.class)
public class NodeConfigurationImpl implements NodeConfiguration {

    private boolean shouldRestartElements = true;

    private List<PlatformRestartHandler> platformRestartHandlers;

    @Override
    public boolean shouldResumeElements() {
        return shouldRestartElements;
    }

    @Override
    public List<PlatformRestartHandler> getPlatformRestartHandlers() {
        return CollectionUtil.emptyOrUnmodifiable(platformRestartHandlers);
    }

    public void setPlatformRestartHandlers(final List<PlatformRestartHandler> platformRestartHandlers) {
        this.platformRestartHandlers = platformRestartHandlers;
    }

    public void setShouldRestartElements(final boolean shouldRestartElements) {
        this.shouldRestartElements = shouldRestartElements;
    }

    @Override
    public boolean shouldClearSessions() {
        return true;
    }

}
