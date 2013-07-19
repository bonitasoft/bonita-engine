package org.bonitasoft.engine.connector;

import java.lang.reflect.Proxy;

import org.bonitasoft.engine.api.APIAccessor;
import org.bonitasoft.engine.api.CommandAPI;
import org.bonitasoft.engine.api.IdentityAPI;
import org.bonitasoft.engine.api.ProcessAPI;
import org.bonitasoft.engine.api.ProfileAPI;
import org.bonitasoft.engine.api.ReportingAPI;
import org.bonitasoft.engine.api.impl.ClientInterceptor;
import org.bonitasoft.engine.api.impl.ServerAPIImpl;
import org.bonitasoft.engine.api.internal.ServerAPI;
import org.bonitasoft.engine.exception.BonitaRuntimeException;
import org.bonitasoft.engine.service.ModelConvertor;
import org.bonitasoft.engine.service.TenantServiceAccessor;
import org.bonitasoft.engine.service.TenantServiceSingleton;
import org.bonitasoft.engine.session.APISession;
import org.bonitasoft.engine.session.SessionService;
import org.bonitasoft.engine.session.model.SSession;
import org.bonitasoft.engine.sessionaccessor.ReadSessionAccessor;

public class ConnectorAPIAccessorImpl implements APIAccessor {

	private final long tenantId;

	public ConnectorAPIAccessorImpl(final long tenantId) {
		super();
		this.tenantId = tenantId;
	}

	private APISession getAPISession() {
		final TenantServiceAccessor tenantServiceAccessor = TenantServiceSingleton.getInstance(tenantId);
		final ReadSessionAccessor readSessionAccessor = tenantServiceAccessor.getReadSessionAccessor();
		final SessionService sessionService = tenantServiceAccessor.getSessionService();
		long sessionId;
		try {
			sessionId = readSessionAccessor.getSessionId();
			final SSession session = sessionService.getSession(sessionId);
			return ModelConvertor.toAPISession(session, null);
		} catch (Exception e) {
			throw new BonitaRuntimeException(e);
		}
	}

	@Override
	public IdentityAPI getIdentityAPI() {
		return getAPI(IdentityAPI.class, getAPISession());
	}

	@Override
	public ProcessAPI getProcessAPI() {
		return getAPI(ProcessAPI.class, getAPISession());
	}

	@Override
	public CommandAPI getCommandAPI() {
		return getAPI(CommandAPI.class, getAPISession());
	}

	@Override
	public ReportingAPI getReportingAPI() {
		return getAPI(ReportingAPI.class, getAPISession());
	}

	@Override
	public ProfileAPI getProfileAPI() {
		return getAPI(ProfileAPI.class, getAPISession());
	}

	private static ServerAPI getServerAPI() {
		return new ServerAPIImpl(false);
	}

	private static <T> T getAPI(final Class<T> clazz, final APISession session) {
		final ServerAPI serverAPI = getServerAPI();
		final ClientInterceptor sessionInterceptor = new ClientInterceptor(clazz.getName(), serverAPI, session);
		return (T) Proxy.newProxyInstance(APIAccessor.class.getClassLoader(), new Class[] { clazz }, sessionInterceptor);
	}

}
