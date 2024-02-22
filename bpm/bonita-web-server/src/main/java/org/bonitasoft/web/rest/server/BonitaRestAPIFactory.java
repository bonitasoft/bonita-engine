/**
 * Copyright (C) 2022 Bonitasoft S.A.
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
package org.bonitasoft.web.rest.server;

import org.bonitasoft.web.rest.server.api.application.APIApplication;
import org.bonitasoft.web.rest.server.api.applicationmenu.APIApplicationMenu;
import org.bonitasoft.web.rest.server.api.applicationpage.APIApplicationDataStoreFactory;
import org.bonitasoft.web.rest.server.api.applicationpage.APIApplicationPage;
import org.bonitasoft.web.rest.server.api.bpm.cases.APIArchivedCase;
import org.bonitasoft.web.rest.server.api.bpm.cases.APIArchivedCaseDocument;
import org.bonitasoft.web.rest.server.api.bpm.cases.APIArchivedComment;
import org.bonitasoft.web.rest.server.api.bpm.cases.APICase;
import org.bonitasoft.web.rest.server.api.bpm.cases.APICaseDocument;
import org.bonitasoft.web.rest.server.api.bpm.cases.APICaseVariable;
import org.bonitasoft.web.rest.server.api.bpm.cases.APIComment;
import org.bonitasoft.web.rest.server.api.bpm.connector.APIArchivedConnectorInstance;
import org.bonitasoft.web.rest.server.api.bpm.connector.APIConnectorInstance;
import org.bonitasoft.web.rest.server.api.bpm.flownode.APIActivity;
import org.bonitasoft.web.rest.server.api.bpm.flownode.APIFlowNode;
import org.bonitasoft.web.rest.server.api.bpm.flownode.APIHumanTask;
import org.bonitasoft.web.rest.server.api.bpm.flownode.APITask;
import org.bonitasoft.web.rest.server.api.bpm.flownode.APIUserTask;
import org.bonitasoft.web.rest.server.api.bpm.flownode.archive.APIArchivedActivity;
import org.bonitasoft.web.rest.server.api.bpm.flownode.archive.APIArchivedFlowNode;
import org.bonitasoft.web.rest.server.api.bpm.flownode.archive.APIArchivedHumanTask;
import org.bonitasoft.web.rest.server.api.bpm.flownode.archive.APIArchivedTask;
import org.bonitasoft.web.rest.server.api.bpm.flownode.archive.APIArchivedUserTask;
import org.bonitasoft.web.rest.server.api.bpm.process.APIActor;
import org.bonitasoft.web.rest.server.api.bpm.process.APIActorMember;
import org.bonitasoft.web.rest.server.api.bpm.process.APICategory;
import org.bonitasoft.web.rest.server.api.bpm.process.APIProcess;
import org.bonitasoft.web.rest.server.api.bpm.process.APIProcessCategory;
import org.bonitasoft.web.rest.server.api.bpm.process.APIProcessConnector;
import org.bonitasoft.web.rest.server.api.bpm.process.APIProcessConnectorDependency;
import org.bonitasoft.web.rest.server.api.bpm.process.APIProcessParameter;
import org.bonitasoft.web.rest.server.api.bpm.process.APIProcessResolutionProblem;
import org.bonitasoft.web.rest.server.api.document.APIArchivedDocument;
import org.bonitasoft.web.rest.server.api.document.APIDocument;
import org.bonitasoft.web.rest.server.api.organization.APICustomUserInfoDefinition;
import org.bonitasoft.web.rest.server.api.organization.APICustomUserInfoUser;
import org.bonitasoft.web.rest.server.api.organization.APICustomUserInfoValue;
import org.bonitasoft.web.rest.server.api.organization.APIGroup;
import org.bonitasoft.web.rest.server.api.organization.APIMembership;
import org.bonitasoft.web.rest.server.api.organization.APIPersonalContactData;
import org.bonitasoft.web.rest.server.api.organization.APIProfessionalContactData;
import org.bonitasoft.web.rest.server.api.organization.APIRole;
import org.bonitasoft.web.rest.server.api.organization.APIUser;
import org.bonitasoft.web.rest.server.api.page.APIPage;
import org.bonitasoft.web.rest.server.api.platform.APIPlatform;
import org.bonitasoft.web.rest.server.api.profile.APIProfile;
import org.bonitasoft.web.rest.server.api.profile.APIProfileMember;
import org.bonitasoft.web.rest.server.api.system.APII18nLocale;
import org.bonitasoft.web.rest.server.api.system.APISession;
import org.bonitasoft.web.rest.server.api.tenant.APITenantAdmin;
import org.bonitasoft.web.rest.server.datastore.application.ApplicationDataStoreCreator;
import org.bonitasoft.web.rest.server.datastore.applicationmenu.ApplicationMenuDataStoreCreator;
import org.bonitasoft.web.rest.server.engineclient.CustomUserInfoEngineClientCreator;
import org.bonitasoft.web.rest.server.framework.API;
import org.bonitasoft.web.rest.server.framework.RestAPIFactory;
import org.bonitasoft.web.toolkit.client.common.exception.api.APINotFoundException;
import org.bonitasoft.web.toolkit.client.data.item.IItem;

/**
 * @author Séverin Moussel
 */
public class BonitaRestAPIFactory extends RestAPIFactory {

    @Override
    public API<? extends IItem> defineApis(final String apiToken, final String resourceToken) {

        if ("identity".equals(apiToken)) {
            if ("user".equals(resourceToken)) {
                return new APIUser();
            } else if ("role".equals(resourceToken)) {
                return new APIRole();
            } else if ("group".equals(resourceToken)) {
                return new APIGroup();
            } else if ("membership".equals(resourceToken)) {
                return new APIMembership();
            } else if ("professionalcontactdata".equals(resourceToken)) {
                return new APIProfessionalContactData();
            } else if ("personalcontactdata".equals(resourceToken)) {
                return new APIPersonalContactData();
            }
        } else if ("customuserinfo".equals(apiToken)) {
            if ("definition".equals(resourceToken)) {
                return new APICustomUserInfoDefinition(new CustomUserInfoEngineClientCreator());
            } else if ("user".equals(resourceToken)) {
                return new APICustomUserInfoUser(new CustomUserInfoEngineClientCreator());
            } else if ("value".equals(resourceToken)) {
                return new APICustomUserInfoValue(new CustomUserInfoEngineClientCreator());
            }
        } else if ("system".equals(apiToken)) {
            if ("i18nlocale".equals(resourceToken)) {
                return new APII18nLocale();
            } else if ("session".equals(resourceToken)) {
                return new APISession();
            } else if ("tenant".equals(resourceToken)) {
                return new APITenantAdmin();
            }

        } else if ("portal".equals(apiToken)) {
            if ("profile".equals(resourceToken)) {
                return new APIProfile();
            } else if ("profileMember".equals(resourceToken)) {
                return new APIProfileMember();
            } else if ("page".equals(resourceToken)) {
                return new APIPage();
            }

        } else if ("bpm".equals(apiToken)) {
            if ("humanTask".equals(resourceToken)) {
                return new APIHumanTask();
            } else if ("userTask".equals(resourceToken)) {
                return new APIUserTask();
            } else if ("archivedHumanTask".equals(resourceToken)) {
                return new APIArchivedHumanTask();
            } else if ("archivedUserTask".equals(resourceToken)) {
                return new APIArchivedUserTask();
            } else if ("process".equals(resourceToken)) {
                return new APIProcess();
            } else if ("category".equals(resourceToken)) {
                return new APICategory();
            } else if ("processCategory".equals(resourceToken)) {
                return new APIProcessCategory();
            } else if ("processConnector".equals(resourceToken)) {
                return new APIProcessConnector();
            } else if ("case".equals(resourceToken)) {
                return new APICase();
            } else if ("archivedCase".equals(resourceToken)) {
                return new APIArchivedCase();
            } else if ("comment".equals(resourceToken)) {
                return new APIComment();
            } else if ("archivedComment".equals(resourceToken)) {
                return new APIArchivedComment();
            } else if ("document".equals(resourceToken)) {
                return new APIDocument();
            } else if ("archiveddocument".equals(resourceToken)) {
                return new APIArchivedDocument();
            } else if ("actor".equals(resourceToken)) {
                return new APIActor();
            } else if ("actorMember".equals(resourceToken)) {
                return new APIActorMember();
            } else if ("delegation".equals(resourceToken)) {
                return new APIActorMember();
            } else if ("activity".equals(resourceToken)) {
                return new APIActivity();
            } else if ("archivedActivity".equals(resourceToken)) {
                return new APIArchivedActivity();
            } else if ("task".equals(resourceToken)) {
                return new APITask();
            } else if ("archivedTask".equals(resourceToken)) {
                return new APIArchivedTask();
            } else if ("flowNode".equals(resourceToken)) {
                return new APIFlowNode();
            } else if ("archivedFlowNode".equals(resourceToken)) {
                return new APIArchivedFlowNode();
            } else if ("processResolutionProblem".equals(resourceToken)) {
                return new APIProcessResolutionProblem();
            } else if ("caseDocument".equals(resourceToken)) {
                return new APICaseDocument();
            } else if ("archivedCaseDocument".equals(resourceToken)) {
                return new APIArchivedCaseDocument();
            } else if ("connectorInstance".equals(resourceToken)) {
                return new APIConnectorInstance();
            } else if ("archivedConnectorInstance".equals(resourceToken)) {
                return new APIArchivedConnectorInstance();
            } else if ("processConnectorDependency".equals(resourceToken)) {
                return new APIProcessConnectorDependency();
            } else if ("caseVariable".equals(resourceToken)) {
                return new APICaseVariable();
            } else if ("processParameter".equals(resourceToken)) {
                return new APIProcessParameter();
            }
        } else if ("living".equals(apiToken)) {
            if ("application".equals(resourceToken)) {
                return new APIApplication(new ApplicationDataStoreCreator(), new APIApplicationDataStoreFactory());
            } else if ("application-page".equals(resourceToken)) {
                return new APIApplicationPage(new APIApplicationDataStoreFactory());
            } else if ("application-menu".equals(resourceToken)) {
                return new APIApplicationMenu(new ApplicationMenuDataStoreCreator());
            }

        } else if ("platform".equals(apiToken)) {
            if ("platform".equals(resourceToken)) {
                return new APIPlatform();
            }
        }
        throw new APINotFoundException(apiToken, resourceToken);
    }
}
