package org.bonitasoft.permissions

import org.bonitasoft.engine.api.APIAccessor
import org.bonitasoft.engine.business.application.Application
import org.bonitasoft.engine.business.application.ApplicationVisibility
import org.bonitasoft.engine.business.application.InternalProfiles
import org.bonitasoft.engine.profile.ProfileCriterion
import org.bonitasoft.engine.session.APISession

class ApplicationPermissionCommon {

    boolean isAppAllowed(Application application, APIAccessor apiAccessor, APISession apiSession) {
        def profileId = application.getProfileId()
        def profileAPI = apiAccessor.getProfileAPI()
        def applicationVisibility = application.getApplicationVisibility()
        if (applicationVisibility != null) {
            if (applicationVisibility == ApplicationVisibility.ALL) {
                return true
            } else if (applicationVisibility == ApplicationVisibility.TECHNICAL_USER) {
                return apiSession.isTechnicalUser()
            }
        }
        def index = 0
        def profile
        def list = []
        while ((list = profileAPI.getProfilesForUser(apiSession.getUserId(), index, 100, ProfileCriterion.ID_ASC)).size() == 100 && (profile = list.find { it.getId() == profileId }) == null) {
            index += 100
        }
        return profile != null || list.find { it.getId() == profileId } != null
    }
}
