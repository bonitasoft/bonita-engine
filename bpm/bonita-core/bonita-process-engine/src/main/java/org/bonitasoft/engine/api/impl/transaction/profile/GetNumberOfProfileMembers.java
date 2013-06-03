/**
 * Copyright (C) 2012 BonitaSoft S.A.
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
package org.bonitasoft.engine.api.impl.transaction.profile;
import java.util.List;

import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.commons.transaction.TransactionContentWithResult;
import org.bonitasoft.engine.profile.ProfileService;
import org.bonitasoft.engine.profile.model.SProfileMember;


/**
 * @author Hongwen Zang
 * 
 */
public class GetNumberOfProfileMembers implements TransactionContentWithResult<List<SProfileMember>> {

    private final List<Long> profileIds;

    private final ProfileService profileService;

    private List<SProfileMember> profileMembers;

    public GetNumberOfProfileMembers(final List<Long> profileIds, final ProfileService profileService) {
        this.profileIds = profileIds;
        this.profileService = profileService;
    }

    @Override
    public void execute() throws SBonitaException {
        profileMembers = profileService.getNumberOfProfileMembers(profileIds);

    }

    @Override
    public List<SProfileMember> getResult() {
        return profileMembers;
    }

}
