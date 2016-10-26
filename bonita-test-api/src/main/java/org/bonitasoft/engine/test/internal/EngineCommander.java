package org.bonitasoft.engine.test.internal;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bonitasoft.engine.api.ApplicationAPI;
import org.bonitasoft.engine.api.BusinessDataAPI;
import org.bonitasoft.engine.api.CommandAPI;
import org.bonitasoft.engine.api.IdentityAPI;
import org.bonitasoft.engine.api.LoginAPI;
import org.bonitasoft.engine.api.PageAPI;
import org.bonitasoft.engine.api.PermissionAPI;
import org.bonitasoft.engine.api.ProcessAPI;
import org.bonitasoft.engine.api.ProfileAPI;
import org.bonitasoft.engine.api.TenantAPIAccessor;
import org.bonitasoft.engine.api.TenantAdministrationAPI;
import org.bonitasoft.engine.api.ThemeAPI;
import org.bonitasoft.engine.bpm.category.Category;
import org.bonitasoft.engine.bpm.category.CategoryCriterion;
import org.bonitasoft.engine.bpm.comment.ArchivedComment;
import org.bonitasoft.engine.bpm.comment.Comment;
import org.bonitasoft.engine.bpm.flownode.ArchivedFlowNodeInstance;
import org.bonitasoft.engine.bpm.flownode.FlowNodeInstance;
import org.bonitasoft.engine.bpm.flownode.WaitingEvent;
import org.bonitasoft.engine.bpm.process.ActivationState;
import org.bonitasoft.engine.bpm.process.ArchivedProcessInstance;
import org.bonitasoft.engine.bpm.process.ProcessDeploymentInfo;
import org.bonitasoft.engine.bpm.process.ProcessDeploymentInfoCriterion;
import org.bonitasoft.engine.bpm.process.ProcessInstance;
import org.bonitasoft.engine.bpm.process.ProcessInstanceCriterion;
import org.bonitasoft.engine.bpm.supervisor.ProcessSupervisor;
import org.bonitasoft.engine.bpm.supervisor.ProcessSupervisorSearchDescriptor;
import org.bonitasoft.engine.command.CommandDescriptor;
import org.bonitasoft.engine.command.CommandNotFoundException;
import org.bonitasoft.engine.command.CommandSearchDescriptor;
import org.bonitasoft.engine.exception.BonitaException;
import org.bonitasoft.engine.exception.BonitaHomeNotSetException;
import org.bonitasoft.engine.exception.DeletionException;
import org.bonitasoft.engine.exception.SearchException;
import org.bonitasoft.engine.exception.ServerAPIException;
import org.bonitasoft.engine.exception.UnknownAPITypeException;
import org.bonitasoft.engine.identity.Group;
import org.bonitasoft.engine.identity.GroupCriterion;
import org.bonitasoft.engine.identity.Role;
import org.bonitasoft.engine.identity.RoleCriterion;
import org.bonitasoft.engine.identity.User;
import org.bonitasoft.engine.identity.UserCriterion;
import org.bonitasoft.engine.search.Order;
import org.bonitasoft.engine.search.SearchOptionsBuilder;
import org.bonitasoft.engine.search.SearchResult;
import org.bonitasoft.engine.session.APISession;
import org.bonitasoft.engine.test.ClientEventUtil;
import org.bonitasoft.engine.test.TestEngineImpl;
import org.slf4j.LoggerFactory;

/**
 * @author Baptiste Mesta
 */
public class EngineCommander {

    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(EngineCommander.class.getName());
    private APISession session;

    private ProcessAPI processAPI;

    private IdentityAPI identityAPI;

    private CommandAPI commandAPI;

    private ProfileAPI profileAPI;

    private ThemeAPI themeAPI;

    private PermissionAPI permissionAPI;

    private PageAPI pageAPI;

    private ApplicationAPI applicationAPI;

    private TenantAdministrationAPI tenantManagementCommunityAPI;

    private BusinessDataAPI businessDataAPI;

    public APISession getSession() {
        return session;
    }

    public void loginOnDefaultTenantWithDefaultTechnicalUser() throws BonitaException {
        final LoginAPI loginAPI = getLoginAPI();
        setSession(loginAPI.login(TestEngineImpl.TECHNICAL_USER_NAME, TestEngineImpl.TECHNICAL_USER_NAME));
        setAPIs();
    }

    protected void setAPIs() throws BonitaHomeNotSetException, ServerAPIException, UnknownAPITypeException {
        setIdentityAPI(TenantAPIAccessor.getIdentityAPI(getSession()));
        setProcessAPI(TenantAPIAccessor.getProcessAPI(getSession()));
        setCommandAPI(TenantAPIAccessor.getCommandAPI(getSession()));
        setProfileAPI(TenantAPIAccessor.getProfileAPI(getSession()));
        setThemeAPI(TenantAPIAccessor.getThemeAPI(getSession()));
        setPermissionAPI(TenantAPIAccessor.getPermissionAPI(getSession()));
        setPageAPI(TenantAPIAccessor.getCustomPageAPI(getSession()));
        setApplicationAPI(TenantAPIAccessor.getLivingApplicationAPI(getSession()));
        setTenantManagementCommunityAPI(TenantAPIAccessor.getTenantAdministrationAPI(getSession()));
        setBusinessDataAPI(TenantAPIAccessor.getBusinessDataAPI(getSession()));
    }

    public void setBusinessDataAPI(final BusinessDataAPI businessDataAPI) {
        this.businessDataAPI = businessDataAPI;
    }

    public void logoutOnTenant() throws BonitaException {
        final LoginAPI loginAPI = getLoginAPI();
        loginAPI.logout(session);
        setSession(null);
        setIdentityAPI(null);
        setProcessAPI(null);
        setCommandAPI(null);
        setProfileAPI(null);
        setThemeAPI(null);
        setPermissionAPI(null);
        setApplicationAPI(null);
        setTenantManagementCommunityAPI(null);
        setPageAPI(null);
        setBusinessDataAPI(null);
    }

    public LoginAPI getLoginAPI() throws BonitaHomeNotSetException, ServerAPIException, UnknownAPITypeException {
        return TenantAPIAccessor.getLoginAPI();
    }

    public BusinessDataAPI getBusinessDataAPI() {
        return businessDataAPI;
    }

    public ProcessAPI getProcessAPI() {
        return processAPI;
    }

    public void setProcessAPI(final ProcessAPI processAPI) {
        this.processAPI = processAPI;
    }

    public IdentityAPI getIdentityAPI() {
        return identityAPI;
    }

    public void setIdentityAPI(final IdentityAPI identityAPI) {
        this.identityAPI = identityAPI;
    }

    public CommandAPI getCommandAPI() {
        return commandAPI;
    }

    public void setCommandAPI(final CommandAPI commandAPI) {
        this.commandAPI = commandAPI;
    }

    public ProfileAPI getProfileAPI() {
        return profileAPI;
    }

    public void setProfileAPI(final ProfileAPI profileAPI) {
        this.profileAPI = profileAPI;
    }

    public ThemeAPI getThemeAPI() {
        return themeAPI;
    }

    public void setThemeAPI(final ThemeAPI themeAPI) {
        this.themeAPI = themeAPI;
    }

    public PermissionAPI getPermissionAPI() {
        return permissionAPI;
    }

    public void setPermissionAPI(final PermissionAPI permissionAPI) {
        this.permissionAPI = permissionAPI;
    }

    public void setSession(final APISession session) {
        this.session = session;
    }

    protected void setPageAPI(final PageAPI pageAPI) {
        this.pageAPI = pageAPI;
    }

    public PageAPI getPageAPI() {
        return pageAPI;
    }

    public ApplicationAPI getApplicationAPI() {
        return applicationAPI;
    }

    public void setApplicationAPI(final ApplicationAPI applicationAPI) {
        this.applicationAPI = applicationAPI;
    }

    public TenantAdministrationAPI getTenantAdministrationAPI() {
        return tenantManagementCommunityAPI;
    }

    public void setTenantManagementCommunityAPI(final TenantAdministrationAPI tenantManagementCommunityAPI) {
        this.tenantManagementCommunityAPI = tenantManagementCommunityAPI;
    }

    public void clearData() throws Exception {
        loginOnDefaultTenantWithDefaultTechnicalUser();
        final List<String> messages = new ArrayList<>();
        messages.addAll(checkNoCommands());
        messages.addAll(checkNoFlowNodes());
        messages.addAll(checkNoArchivedFlowNodes());
        messages.addAll(checkNoComments());
        messages.addAll(checkNoArchivedComments());
        messages.addAll(checkNoWaitingEvent());
        messages.addAll(checkNoProcessIntances());
        messages.addAll(checkNoArchivedProcessIntances());
        messages.addAll(checkNoProcessDefinitions());
        messages.addAll(checkNoCategories());
        messages.addAll(checkNoUsers());
        messages.addAll(checkNoGroups());
        messages.addAll(checkNoRoles());
        messages.addAll(checkNoSupervisors());
        logoutOnTenant();
        LOGGER.warn("Engine was not clean after test:");
        for (String message : messages) {
            LOGGER.warn(message);
        }
    }

    public List<String> checkNoCategories() throws DeletionException {
        final List<String> messages = new ArrayList<>();
        final long numberOfCategories = getProcessAPI().getNumberOfCategories();
        if (numberOfCategories > 0) {
            final List<Category> categories = getProcessAPI().getCategories(0, 5000, CategoryCriterion.NAME_ASC);
            final StringBuilder categoryBuilder = new StringBuilder("Categories are still present: ");
            for (final Category category : categories) {
                categoryBuilder.append(category.getName()).append(", ");
                getProcessAPI().deleteCategory(category.getId());
            }
            messages.add(categoryBuilder.toString());
        }
        return messages;
    }

    public List<String> checkNoFlowNodes() throws SearchException {
        final List<String> messages = new ArrayList<>();
        final SearchOptionsBuilder build = new SearchOptionsBuilder(0, 1000);
        final SearchResult<FlowNodeInstance> searchResult = getProcessAPI().searchFlowNodeInstances(build.done());
        final List<FlowNodeInstance> flowNodeInstances = searchResult.getResult();
        if (searchResult.getCount() > 0) {
            final StringBuilder messageBuilder = new StringBuilder("FlowNodes are still present: ");
            for (final FlowNodeInstance flowNodeInstance : flowNodeInstances) {
                messageBuilder.append("{").append(flowNodeInstance.getName()).append(" - ").append(flowNodeInstance.getType()).append("}").append(", ");
            }
            messages.add(messageBuilder.toString());
        }
        return messages;
    }

    public List<String> checkNoArchivedFlowNodes() throws SearchException {
        final List<String> messages = new ArrayList<>();
        final SearchOptionsBuilder build = new SearchOptionsBuilder(0, 1000);
        final SearchResult<ArchivedFlowNodeInstance> searchResult = getProcessAPI().searchArchivedFlowNodeInstances(build.done());
        final List<ArchivedFlowNodeInstance> archivedFlowNodeInstances = searchResult.getResult();
        if (searchResult.getCount() > 0) {
            final StringBuilder messageBuilder = new StringBuilder("Archived flowNodes are still present: ");
            for (final ArchivedFlowNodeInstance archivedFlowNodeInstance : archivedFlowNodeInstances) {
                messageBuilder.append(archivedFlowNodeInstance.getName()).append(", ");
            }
            messages.add(messageBuilder.toString());
        }
        return messages;
    }

    public List<String> checkNoComments() throws SearchException {
        final List<String> messages = new ArrayList<>();
        final SearchOptionsBuilder build = new SearchOptionsBuilder(0, 1000);
        final SearchResult<Comment> searchResult = getProcessAPI().searchComments(build.done());
        final List<Comment> comments = searchResult.getResult();
        if (searchResult.getCount() > 0) {
            final StringBuilder messageBuilder = new StringBuilder("Comments are still present: ");
            for (final Comment comment : comments) {
                messageBuilder.append(comment.getContent()).append(", ");
            }
            messages.add(messageBuilder.toString());
        }
        return messages;
    }

    public List<String> checkNoArchivedComments() throws SearchException {
        final List<String> messages = new ArrayList<>();
        final SearchOptionsBuilder build = new SearchOptionsBuilder(0, 1000);
        final SearchResult<ArchivedComment> searchResult = getProcessAPI().searchArchivedComments(build.done());
        final List<ArchivedComment> archivedComments = searchResult.getResult();
        if (searchResult.getCount() > 0) {
            final StringBuilder messageBuilder = new StringBuilder("Archived comments are still present: ");
            for (final ArchivedComment archivedComment : archivedComments) {
                messageBuilder.append(archivedComment.getName()).append(", ");
            }
            messages.add(messageBuilder.toString());
        }
        return messages;
    }

    public List<String> checkNoProcessDefinitions() throws BonitaException {
        final List<String> messages = new ArrayList<>();
        final List<ProcessDeploymentInfo> processes = getProcessAPI().getProcessDeploymentInfos(0, 200, ProcessDeploymentInfoCriterion.DEFAULT);
        if (processes.size() > 0) {
            final StringBuilder processBuilder = new StringBuilder("Process Definitions are still active: ");
            for (final ProcessDeploymentInfo processDeploymentInfo : processes) {
                processBuilder.append(processDeploymentInfo.getId()).append(", ");
                if (ActivationState.ENABLED.equals(processDeploymentInfo.getActivationState())) {
                    getProcessAPI().disableProcess(processDeploymentInfo.getProcessId());
                }
                getProcessAPI().deleteProcessDefinition(processDeploymentInfo.getProcessId());
            }
            messages.add(processBuilder.toString());
        }
        return messages;
    }

    public List<String> checkNoWaitingEvent() throws BonitaException {
        final List<String> messages = new ArrayList<>();
        final Map<String, Serializable> parameters = new HashMap<>(1);
        parameters.put("searchOptions", new SearchOptionsBuilder(0, 200).done());

        @SuppressWarnings("unchecked")
        final List<WaitingEvent> waitingEvents = ((SearchResult<WaitingEvent>) getCommandAPI().execute("searchWaitingEventsCommand", parameters)).getResult();
        if (waitingEvents.size() > 0) {
            final StringBuilder messageBuilder = new StringBuilder("Waiting Event are still present: ");
            for (final WaitingEvent waitingEvent : waitingEvents) {
                messageBuilder.append("[process instance:").append(waitingEvent.getProcessName()).append(", flow node instance:")
                        .append(waitingEvent.getFlowNodeInstanceId()).append("]").append(
                                ", ");
            }
            messages.add(messageBuilder.toString());
        }
        return messages;
    }

    public List<String> checkNoSupervisors() throws SearchException, DeletionException {
        final List<String> messages = new ArrayList<>();
        final SearchOptionsBuilder builder = new SearchOptionsBuilder(0, 200);
        builder.sort(ProcessSupervisorSearchDescriptor.ID, Order.ASC);
        final List<ProcessSupervisor> supervisors = getProcessAPI().searchProcessSupervisors(builder.done()).getResult();

        if (supervisors.size() > 0) {
            final StringBuilder processBuilder = new StringBuilder("Process Supervisors are still active: ");
            for (final ProcessSupervisor supervisor : supervisors) {
                processBuilder.append(supervisor.getSupervisorId()).append(", ");
                getProcessAPI().deleteSupervisor(supervisor.getSupervisorId());
            }
            messages.add(processBuilder.toString());
        }
        return messages;
    }

    public List<String> checkNoProcessIntances() throws DeletionException {
        final List<String> messages = new ArrayList<>();
        final List<ProcessInstance> processInstances = getProcessAPI().getProcessInstances(0, 1000, ProcessInstanceCriterion.DEFAULT);
        if (!processInstances.isEmpty()) {
            final StringBuilder stb = new StringBuilder("Process instances are still present: ");
            for (final ProcessInstance processInstance : processInstances) {
                stb.append(processInstance).append(", ");
                getProcessAPI().deleteProcessInstance(processInstance.getId());
            }
            messages.add(stb.toString());
        }
        return messages;
    }

    public List<String> checkNoArchivedProcessIntances() throws DeletionException {
        final List<String> messages = new ArrayList<>();
        final List<ArchivedProcessInstance> archivedProcessInstances = getProcessAPI().getArchivedProcessInstances(0, 1000, ProcessInstanceCriterion.DEFAULT);
        if (!archivedProcessInstances.isEmpty()) {
            final StringBuilder stb = new StringBuilder("Archived process instances are still present: ");
            for (final ArchivedProcessInstance archivedProcessInstance : archivedProcessInstances) {
                stb.append(archivedProcessInstance).append(", ");
                getProcessAPI().deleteArchivedProcessInstancesInAllStates(archivedProcessInstance.getSourceObjectId());
            }
            messages.add(stb.toString());
        }
        return messages;
    }

    public List<String> checkNoGroups() throws DeletionException {
        final List<String> messages = new ArrayList<>();
        final long numberOfGroups = getIdentityAPI().getNumberOfGroups();
        if (numberOfGroups > 0) {
            final List<Group> groups = getIdentityAPI().getGroups(0, Long.valueOf(numberOfGroups).intValue(), GroupCriterion.NAME_ASC);
            final StringBuilder groupBuilder = new StringBuilder("Groups are still present: ");
            for (final Group group : groups) {
                groupBuilder.append(group.getId()).append(", ");
                getIdentityAPI().deleteGroup(group.getId());
            }
            messages.add(groupBuilder.toString());
        }
        return messages;
    }

    public List<String> checkNoRoles() throws DeletionException {
        final List<String> messages = new ArrayList<>();
        final long numberOfRoles = getIdentityAPI().getNumberOfRoles();
        if (numberOfRoles > 0) {
            final List<Role> roles = getIdentityAPI().getRoles(0, Long.valueOf(numberOfRoles).intValue(), RoleCriterion.NAME_ASC);
            final StringBuilder roleBuilder = new StringBuilder("Roles are still present: ");
            for (final Role role : roles) {
                roleBuilder.append(role.getId()).append(", ");
                getIdentityAPI().deleteRole(role.getId());
            }
            messages.add(roleBuilder.toString());
        }
        return messages;
    }

    public List<String> checkNoUsers() throws DeletionException {
        final List<String> messages = new ArrayList<>();
        final long numberOfUsers = getIdentityAPI().getNumberOfUsers();
        if (numberOfUsers > 0) {
            final List<User> users = getIdentityAPI().getUsers(0, Long.valueOf(numberOfUsers).intValue(), UserCriterion.USER_NAME_ASC);
            final StringBuilder userBuilder = new StringBuilder("Users are still present: ");
            for (final User user : users) {
                userBuilder.append(user.getId()).append(", ");
                getIdentityAPI().deleteUser(user.getId());
            }
            messages.add(userBuilder.toString());
        }
        return messages;
    }

    public List<String> checkNoCommands() throws SearchException, CommandNotFoundException, DeletionException {
        final List<String> messages = new ArrayList<>();
        final SearchOptionsBuilder searchOptionsBuilder = new SearchOptionsBuilder(0, 1000);
        searchOptionsBuilder.filter(CommandSearchDescriptor.SYSTEM, false);
        searchOptionsBuilder.differentFrom(CommandSearchDescriptor.NAME, ClientEventUtil.ADD_HANDLER_COMMAND);
        searchOptionsBuilder.differentFrom(CommandSearchDescriptor.NAME, ClientEventUtil.WAIT_SERVER_COMMAND);
        final SearchResult<CommandDescriptor> searchCommands = getCommandAPI().searchCommands(searchOptionsBuilder.done());
        final List<CommandDescriptor> commands = searchCommands.getResult();
        if (searchCommands.getCount() > 0) {
            final StringBuilder commandBuilder = new StringBuilder("Commands are still present: ");
            for (final CommandDescriptor command : commands) {
                commandBuilder.append(command.getName()).append(", ");
                getCommandAPI().unregister(command.getName());
            }
            messages.add(commandBuilder.toString());
        }
        return messages;
    }

}
