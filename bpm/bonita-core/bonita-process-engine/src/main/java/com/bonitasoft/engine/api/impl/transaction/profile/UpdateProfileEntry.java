/**
 * Copyright (C) 2011-2013 BonitaSoft S.A.
 * BonitaSoft, 31 rue Gustave Eiffel - 38000 Grenoble
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2.0 of the License, or
 * (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.bonitasoft.engine.api.impl.transaction.profile;

import java.io.Serializable;
import java.util.Map;
import java.util.Map.Entry;

import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.commons.transaction.TransactionContentWithResult;
import org.bonitasoft.engine.profile.ProfileService;
import org.bonitasoft.engine.profile.builder.SProfileEntryUpdateBuilder;
import org.bonitasoft.engine.profile.model.SProfileEntry;
import org.bonitasoft.engine.recorder.model.EntityUpdateDescriptor;

import com.bonitasoft.engine.bpm.model.ProfileEntryUpdateDescriptor;
import com.bonitasoft.engine.bpm.model.ProfileEntryUpdateDescriptor.ProfileEntryField;

/**
 * @author Julien Mege
 * @author Celine Souchet
 */
public class UpdateProfileEntry implements TransactionContentWithResult<SProfileEntry> {

    private final ProfileService profileService;

    private final Long profileEntryId;

    private final ProfileEntryUpdateDescriptor updateDescriptor;

    private final SProfileEntryUpdateBuilder updateBuilder;

    private SProfileEntry sProfileEntry = null;

    public UpdateProfileEntry(final ProfileService profileService, final SProfileEntryUpdateBuilder updateBuilder, final Long profileEntryId,
            final ProfileEntryUpdateDescriptor updateDescriptor) {
        super();
        this.profileService = profileService;
        this.profileEntryId = profileEntryId;
        this.updateDescriptor = updateDescriptor;
        this.updateBuilder = updateBuilder;
    }

    @Override
    public void execute() throws SBonitaException {
        sProfileEntry = profileService.getProfileEntry(profileEntryId);
        profileService.updateProfileEntry(sProfileEntry, getProfileEntryUpdateDescriptor());
        sProfileEntry = profileService.getProfileEntry(profileEntryId);
    }

    @Override
    public SProfileEntry getResult() {
        return sProfileEntry;
    }

    private EntityUpdateDescriptor getProfileEntryUpdateDescriptor() {
        final Map<ProfileEntryField, Serializable> fields = updateDescriptor.getFields();
        for (final Entry<ProfileEntryField, Serializable> field : fields.entrySet()) {
            switch (field.getKey()) {
                case NAME:
                    updateBuilder.setName((String) field.getValue());
                    break;
                case DESCRIPTION:
                    updateBuilder.setDescription((String) field.getValue());
                    break;
                case PARENT_ID:
                    updateBuilder.setParentId((Long) field.getValue());
                    break;
                case PROFILE_ID:
                    updateBuilder.setProfileId((Long) field.getValue());
                    break;
                case INDEX:
                    updateBuilder.setIndex((Long) field.getValue());
                    break;
                case TYPE:
                    updateBuilder.setType((String) field.getValue());
                    break;
                case PAGE:
                    updateBuilder.setPage((String) field.getValue());
                    break;
            }
        }
        return updateBuilder.done();
    }

}
