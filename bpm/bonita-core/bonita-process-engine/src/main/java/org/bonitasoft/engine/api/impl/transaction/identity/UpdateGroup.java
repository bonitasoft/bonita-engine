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
 **/
package org.bonitasoft.engine.api.impl.transaction.identity;

import java.util.List;
import java.util.Map;

import org.bonitasoft.engine.builder.BuilderFactory;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.commons.transaction.TransactionContent;
import org.bonitasoft.engine.identity.IdentityService;
import org.bonitasoft.engine.identity.SIdentityException;
import org.bonitasoft.engine.identity.model.SGroup;
import org.bonitasoft.engine.identity.model.builder.SGroupBuilderFactory;
import org.bonitasoft.engine.persistence.OrderByType;
import org.bonitasoft.engine.recorder.model.EntityUpdateDescriptor;

/**
 * @author Lu Kai
 * @author Matthieu Chaffotte
 * @author Elias Ricken de Medeiros
 */
public class UpdateGroup implements TransactionContent {

    private static final int BATCH_SIZE = 100;

    private final long groupId;

    private final EntityUpdateDescriptor changeDescriptor;

    private final IdentityService identityService;

    private SGroup sGroup;

    public UpdateGroup(final long groupId, final EntityUpdateDescriptor changeDescriptor, final IdentityService identityService) {
        this.groupId = groupId;
        this.changeDescriptor = changeDescriptor;
        this.identityService = identityService;
    }

    @Override
    public void execute() throws SBonitaException {
        sGroup = identityService.getGroup(groupId);

        updateGroup(sGroup, changeDescriptor);
    }

    private void updateGroup(final SGroup group, final EntityUpdateDescriptor changeDescriptor) throws SIdentityException {
        final SGroupBuilderFactory sGroupFactiry = BuilderFactory.get(SGroupBuilderFactory.class);
        // if the parent path changes it's also necessary to change the children's parent path
        final String parentPathKey = sGroupFactiry.getParentPathKey();
        final String nameKey = sGroupFactiry.getNameKey();
        final Map<String, Object> fields = changeDescriptor.getFields();
        if (fields.containsKey(parentPathKey) || fields.containsKey(nameKey)) {
            final String parentPath = fields.containsKey(parentPathKey) ? (String) fields.get(parentPathKey) : group.getParentPath();
            final String groupName = fields.containsKey(nameKey) ? (String) fields.get(nameKey) : group.getName();
            updateChildren(group, parentPath, sGroupFactiry.getIdKey(), parentPathKey, groupName);
        }
        identityService.updateGroup(group, changeDescriptor);
    }

    private void updateChildren(final SGroup group, final String parentPath, final String idKey, final String parentPathKey, final String groupName)
            throws SIdentityException {
        List<SGroup> groupChildren = null;
        final StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(parentPath);
        stringBuilder.append('/');
        stringBuilder.append(groupName);
        final String parentPath2 = stringBuilder.toString();
        int i = 0;
        do {
            groupChildren = identityService.getGroupChildren(group.getId(), i * BATCH_SIZE, BATCH_SIZE, idKey, OrderByType.ASC);
            i++;
            for (final SGroup child : groupChildren) {
                updateChildren(child, parentPath2, idKey, parentPathKey, child.getName());
            }
        } while (!groupChildren.isEmpty());
        final EntityUpdateDescriptor updateDescriptor = new EntityUpdateDescriptor();
        updateDescriptor.addField(parentPathKey, parentPath);
        identityService.updateGroup(group, updateDescriptor);

    }

}
