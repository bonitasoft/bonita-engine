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
public class UpdateGroup {

    private static final int BATCH_SIZE = 100;

    private final long groupId;

    private final EntityUpdateDescriptor changeDescriptor;

    private final IdentityService identityService;
    private EntityUpdateDescriptor iconUpdater;

    public UpdateGroup(final long groupId, final EntityUpdateDescriptor changeDescriptor, final IdentityService identityService,
            EntityUpdateDescriptor iconUpdater) {
        this.groupId = groupId;
        this.changeDescriptor = changeDescriptor;
        this.identityService = identityService;
        this.iconUpdater = iconUpdater;
    }

    public SGroup update() throws SIdentityException {
        SGroup sGroup = identityService.getGroup(groupId);

        final SGroupBuilderFactory sGroupBuilderFactory = BuilderFactory.get(SGroupBuilderFactory.class);
        // if the parent path changes it's also necessary to change the children's parent path
        final String parentPathKey = sGroupBuilderFactory.getParentPathKey();
        final String nameKey = sGroupBuilderFactory.getNameKey();
        final Map<String, Object> fields = changeDescriptor.getFields();
        if (fields.containsKey(parentPathKey) || fields.containsKey(nameKey)) {
            final String parentPath = fields.containsKey(parentPathKey) ? (String) fields.get(parentPathKey) : sGroup.getParentPath();
            final String groupName = fields.containsKey(nameKey) ? (String) fields.get(nameKey) : sGroup.getName();
            updateChildren(sGroup, parentPath, sGroupBuilderFactory.getIdKey(), parentPathKey, groupName);
        }
        identityService.updateGroup(sGroup, changeDescriptor, iconUpdater);
        return sGroup;
    }

    private void updateChildren(final SGroup group, final String parentPath, final String idKey, final String parentPathKey, final String groupName)
            throws SIdentityException {
        List<SGroup> groupChildren;
        int i = 0;
        do {
            groupChildren = identityService.getGroupChildren(group.getId(), i * BATCH_SIZE, BATCH_SIZE, idKey, OrderByType.ASC);
            i++;
            for (final SGroup child : groupChildren) {
                if (parentPath != null) {
                    updateChildren(child, parentPath + '/' + groupName, idKey, parentPathKey, child.getName());
                } else {
                    updateChildren(child, '/' + groupName, idKey, parentPathKey, child.getName());
                }
            }
        } while (!groupChildren.isEmpty());
        final EntityUpdateDescriptor updateDescriptor = new EntityUpdateDescriptor();
        updateDescriptor.addField(parentPathKey, parentPath);
        identityService.updateGroup(group, updateDescriptor, null);

    }

}
