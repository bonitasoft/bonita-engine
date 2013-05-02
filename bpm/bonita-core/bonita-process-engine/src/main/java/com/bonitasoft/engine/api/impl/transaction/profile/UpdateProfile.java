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
import org.bonitasoft.engine.profile.builder.SProfileUpdateBuilder;
import org.bonitasoft.engine.profile.model.SProfile;
import org.bonitasoft.engine.recorder.model.EntityUpdateDescriptor;

import com.bonitasoft.engine.bpm.model.ProfileUpdateDescriptor;
import com.bonitasoft.engine.bpm.model.ProfileUpdateDescriptor.ProfileField;

/**
 * @author Julien Mege
 * @author Celine Souchet
 */
public class UpdateProfile implements TransactionContentWithResult<SProfile> {

    private final ProfileService profileService;

    private final Long profileId;

    private final ProfileUpdateDescriptor updateDescriptor;

    private final SProfileUpdateBuilder updateBuilder;

    private SProfile profile = null;

    public UpdateProfile(final ProfileService profileService, final SProfileUpdateBuilder updateBuilder, final Long profileId,
            final ProfileUpdateDescriptor updateDescriptor) {
        super();
        this.profileService = profileService;
        this.profileId = profileId;
        this.updateDescriptor = updateDescriptor;
        this.updateBuilder = updateBuilder;
    }

    @Override
    public void execute() throws SBonitaException {
        profile = profileService.getProfile(profileId);
        profileService.updateProfile(profile, getProfileUpdateDescriptor());
        profile = profileService.getProfile(profileId);
    }

    @Override
    public SProfile getResult() {
        return this.profile;
    }

    private EntityUpdateDescriptor getProfileUpdateDescriptor() {
        final Map<ProfileField, Serializable> fields = updateDescriptor.getFields();
        for (final Entry<ProfileField, Serializable> field : fields.entrySet()) {
            switch (field.getKey()) {
                case NAME:
                    updateBuilder.setName((String) field.getValue());
                    break;
                case DESCRIPTION:
                    updateBuilder.setDescription((String) field.getValue());
                    break;
                case ICON_PATH:
                    updateBuilder.setIconPath((String) field.getValue());
                    break;
            }
        }
        return updateBuilder.done();
    }

}
