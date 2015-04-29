/**
 * Copyright (C) 2015 BonitaSoft S.A.
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
package org.bonitasoft.engine.archive.impl;


import java.util.Map;

/**
 * @author Matthieu Chaffotte
 * @author Celine Souchet
 */
public class DefaultArchivingStrategy extends AbstractArchivingStrategy {

    public DefaultArchivingStrategy(Map<String, Boolean> additionalConfiguration) {
        super();
        archives.put("org.bonitasoft.engine.core.process.comment.model.SComment", true);
        archives.put("org.bonitasoft.engine.core.document.model.SDocumentMapping", true);
        archives.put("org.bonitasoft.engine.core.process.instance.model.SProcessInstance", true);
        archives.put("org.bonitasoft.engine.core.process.instance.model.STransitionInstance", true);
        archives.put("org.bonitasoft.engine.core.process.instance.model.SAutomaticTaskInstance", true);
        archives.put("org.bonitasoft.engine.core.process.instance.model.SReceiveTaskInstance", true);
        archives.put("org.bonitasoft.engine.core.process.instance.model.SManualTaskInstance", true);
        archives.put("org.bonitasoft.engine.core.process.instance.model.SUserTaskInstance", true);
        archives.put("org.bonitasoft.engine.core.process.instance.model.SLoopActivityInstance", true);
        archives.put("org.bonitasoft.engine.core.process.instance.model.SMultiInstanceActivityInstance", true);
        archives.put("org.bonitasoft.engine.core.process.instance.model.SCallActivityInstance", true);
        archives.put("org.bonitasoft.engine.core.process.instance.model.SGatewayInstance", true);
        archives.put("org.bonitasoft.engine.core.process.instance.model.SSubProcessActivityInstance", true);
        archives.put("org.bonitasoft.engine.core.process.instance.model.SConnectorInstance", true);
        archives.put("org.bonitasoft.engine.core.process.instance.model.SSendTaskInstance", true);
        archives.put("org.bonitasoft.engine.core.process.instance.model.SReceiveTaskInstance", true);
        archives.put("org.bonitasoft.engine.data.instance.model.SDataInstance", true);
        // is the archived version because there is no not archived version
        archives.put("org.bonitasoft.engine.core.process.instance.model.archive.SATransitionInstance", true);
        for (Map.Entry<String, Boolean> entry : additionalConfiguration.entrySet()) {
            if (!archives.containsKey(entry.getKey())) {
                archives.put(entry.getKey(), entry.getValue());
            }
        }
    }

}
