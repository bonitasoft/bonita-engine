/*
 * Copyright (C) 2016 BonitaSoft S.A.
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
 */
package org.bonitasoft.engine.core.form.impl.custom;

import static org.bonitasoft.engine.page.AuthorizationRuleConstants.*;

import java.util.Arrays;
import java.util.List;

import org.bonitasoft.engine.core.form.AuthorizationRuleMapping;

/**
 * @author Laurent Leseigneur
 */
public class CustomAuthorizationRuleMappingImpl implements AuthorizationRuleMapping {

    @Override
    public List<String> getProcessStartRuleKeys() {
        return Arrays.asList(IS_ADMIN, IS_PROCESS_OWNER, IS_ACTOR_INITIATOR);
    }

    @Override
    public List<String> getProcessOverviewRuleKeys() {
        return Arrays.asList(IS_ADMIN, IS_PROCESS_OWNER, IS_PROCESS_INITIATOR, IS_TASK_PERFORMER, IS_INVOLVED_IN_PROCESS_INSTANCE,
                IS_PROCESS_INITIATOR + "_CUSTOM");
    }

    @Override
    public List<String> getTaskRuleKeys() {
        return Arrays.asList(IS_ADMIN, IS_PROCESS_OWNER, IS_TASK_AVAILABLE_FOR_USER);
    }
}
