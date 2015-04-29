/*******************************************************************************
 * Copyright (C) 2015 Bonitasoft S.A.
 * Bonitasoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * Bonitasoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.authentication.impl.cas;

import static org.assertj.core.api.Assertions.*;
import static org.junit.Assert.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.Serializable;
import java.io.StringWriter;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.security.auth.login.LoginException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.io.FileUtils;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.HttpParams;
import org.bonitasoft.engine.authentication.AuthenticationConstants;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;
import org.w3c.dom.Document;

@RunWith(MockitoJUnitRunner.class)
public class CASAuthenticatorImplTest {

    public static final String LINE_SEPARATOR = System.getProperty("line.separator");
    private static final String REGEX_LINEBREAK_ALL_PLATFORMS = "\\r\\n|\\r|\\n";

    private final String casService = "http://bonitasoft.com/bonita";

    private final String casURLPRefix = "http://cas.bonitasoft.com/cas";

    private Map<String, Serializable> credentials;

    @Mock
    private TechnicalLoggerService logger;

    @Mock
    private DefaultHttpClient hc;

    @Mock
    private Document document;

    @Mock
    private HttpPost postRequest;

    @Mock
    private HttpGet getRequest;

    @Mock
    private HttpResponse response;

    @Mock
    private Header header;

    @Mock
    private CASUtils casUtils;

    @Spy
    @InjectMocks
    private CASAuthenticatorImpl casAuthenticatorImpl;

    @Before
    public void before() {
        casAuthenticatorImpl.setCasService(casService);
        casAuthenticatorImpl.setCasServerUrlPrefix(casURLPRefix);
        casAuthenticatorImpl.casUtils = casUtils;
        credentials = new HashMap<String, Serializable>();
    }

    @Test
    public void testAuthenticateShouldRaiseLicenseError() {
        Exception e = new IllegalStateException(CASUtils.THE_CAS_AUTHENTICATOR_IS_NOT_AN_ACTIVE_FEATURE);
        doThrow(e).when(casUtils).checkLicense();
        try {
            casAuthenticatorImpl.authenticate(credentials);
        } catch (IllegalStateException raisedException) {
            assertThat(raisedException).isSameAs(e);
            verify(casAuthenticatorImpl, never()).loginToServiceViaCAS(credentials);
            return;
        }
        fail();
    }

    @Test
    public void testAuthenticate() {
        doAnswer(new Answer<Object>() {

            @Override
            public Object answer(InvocationOnMock invocation) {
                return null;
            }
        }).when(casUtils).checkLicense();

        doAnswer(new Answer<Object>() {

            @Override
            public Object answer(InvocationOnMock invocation) {
                return null;
            }
        }).when(casAuthenticatorImpl).loginToServiceViaCAS(credentials);
        casAuthenticatorImpl.authenticate(credentials);
        verify(casAuthenticatorImpl, times(1)).loginToServiceViaCAS(credentials);
    }

    @Test
    public void testRetrievePasswordFromCredentials() throws Exception {
        String password = "password";
        credentials.put(AuthenticationConstants.BASIC_PASSWORD, password);
        String passwordFound = casAuthenticatorImpl.retrievePasswordFromCredentials(credentials);
        assertThat(passwordFound).isSameAs(password);
    }

    @Test
    public void testRetrieveEmptyPasswordFromCredentials() {
        try {
            casAuthenticatorImpl.retrievePasswordFromCredentials(credentials);
        } catch (LoginException e) {
            assertThat(e).hasMessage("invalid credentials, password is absent");
            return;
        }
        fail();
    }

    @Test
    public void testRetrieveEmptyUsernameFromCredentials() {
        String login = "";
        credentials.put(AuthenticationConstants.BASIC_USERNAME, login);
        try {
            casAuthenticatorImpl.retrieveUsernameFromCredentials(credentials);
        } catch (LoginException e) {
            assertThat(e).hasMessage("invalid credentials, username is blank");
            return;
        }
        fail();
    }

    @Test
    public void testRetrieveNullUsernameFromCredentials() {
        try {
            casAuthenticatorImpl.retrieveUsernameFromCredentials(credentials);
        } catch (LoginException e) {
            assertThat(e).hasMessage("invalid credentials, username is blank");
            return;
        }
        fail();
    }

    @Test
    public void testRetrieveUsernameFromCredentials() throws Exception {
        String login = "username";
        credentials.put(AuthenticationConstants.BASIC_USERNAME, login);
        String loginFound = casAuthenticatorImpl.retrieveUsernameFromCredentials(credentials);
        assertThat(loginFound).isSameAs(login);
    }

    @Test
    public void testLoginToServiceViaCAS() throws Exception {
        String login = "username";
        String password = "password";
        Long tenantId = 1L;
        credentials.put(AuthenticationConstants.BASIC_TENANT_ID, tenantId);
        doReturn(login).when(casAuthenticatorImpl).retrieveUsernameFromCredentials(credentials);
        doReturn(password).when(casAuthenticatorImpl).retrievePasswordFromCredentials(credentials);
        doReturn(hc).when(casAuthenticatorImpl).createDefaultHttpClient();
        String content = "some content";
        doReturn(content).when(casAuthenticatorImpl).retrieveCASLoginPage(hc);
        doReturn(document).when(casAuthenticatorImpl).createDOMDocumentFromResponse(content);
        String lt = "LT-sdfmlkjfg";
        doReturn(lt).when(casAuthenticatorImpl).extractLtInputFromDOMDocument(document);
        String action = "http://cas/actionSubmit";
        doReturn(action).when(casAuthenticatorImpl).extractFormActionFromDOMDocument(document);

        doReturn(postRequest).when(casAuthenticatorImpl).createCasAuthenticationHttpRequest(lt, action, login, password);
        Header[] headers = new Header[] { header };
        doReturn(headers).when(casAuthenticatorImpl).authenticateOnCASAndRetrieveCookies(hc, postRequest);
        doReturn(getRequest).when(casAuthenticatorImpl).createHttpRequestForTicketRetrieval(action, headers);
        HttpParams hp = mock(HttpParams.class);
        doReturn(hp).when(casAuthenticatorImpl).getHttpParams(hc);

        doReturn(response).when(casAuthenticatorImpl).executeTicketRequest(hc, getRequest);
        String ticket = "ST-sdgdfkjldkfjg";
        doReturn(ticket).when(casAuthenticatorImpl).retrieveTicketFromRedirect(response);
        StatusLine sl = mock(StatusLine.class);
        when(response.getStatusLine()).thenReturn(sl);
        when(sl.getStatusCode()).thenReturn(302);
        Map<String, Serializable> jaasCredentials = casAuthenticatorImpl.loginToServiceViaCAS(credentials);

        verify(casAuthenticatorImpl, times(1)).retrieveUsernameFromCredentials(credentials);
        verify(casAuthenticatorImpl, times(1)).retrievePasswordFromCredentials(credentials);
        verify(casAuthenticatorImpl, times(1)).createDefaultHttpClient();
        verify(casAuthenticatorImpl, times(1)).retrieveCASLoginPage(hc);
        verify(casAuthenticatorImpl, times(1)).createDOMDocumentFromResponse(content);
        verify(casAuthenticatorImpl, times(1)).extractLtInputFromDOMDocument(document);
        verify(casAuthenticatorImpl, times(1)).extractFormActionFromDOMDocument(document);

        verify(casAuthenticatorImpl, times(1)).createCasAuthenticationHttpRequest(lt, action, login, password);
        verify(casAuthenticatorImpl, times(1)).authenticateOnCASAndRetrieveCookies(hc, postRequest);
        verify(casAuthenticatorImpl, times(1)).createHttpRequestForTicketRetrieval(action, headers);
        verify(casAuthenticatorImpl, times(1)).getHttpParams(hc);

        verify(casAuthenticatorImpl, times(1)).executeTicketRequest(hc, getRequest);
        verify(casAuthenticatorImpl, times(1)).retrieveTicketFromRedirect(response);

        verify(response, times(2)).getStatusLine();
        verify(sl, times(1)).getStatusCode();
        assertThat(jaasCredentials).containsEntry("ticket", ticket).containsEntry(AuthenticationConstants.BASIC_TENANT_ID, tenantId);
    }

    @Test
    public void testLoginToServiceViaCASFails() throws Exception {
        String login = "username";
        String password = "password";
        doReturn(login).when(casAuthenticatorImpl).retrieveUsernameFromCredentials(credentials);
        doReturn(password).when(casAuthenticatorImpl).retrievePasswordFromCredentials(credentials);
        doReturn(hc).when(casAuthenticatorImpl).createDefaultHttpClient();
        String content = "some content";
        doReturn(content).when(casAuthenticatorImpl).retrieveCASLoginPage(hc);
        doReturn(document).when(casAuthenticatorImpl).createDOMDocumentFromResponse(content);
        String lt = "LT-sdfmlkjfg";
        doReturn(lt).when(casAuthenticatorImpl).extractLtInputFromDOMDocument(document);
        String action = "http://cas/actionSubmit";
        doReturn(action).when(casAuthenticatorImpl).extractFormActionFromDOMDocument(document);

        doReturn(postRequest).when(casAuthenticatorImpl).createCasAuthenticationHttpRequest(lt, action, login, password);
        Header[] headers = new Header[] { header };
        doReturn(headers).when(casAuthenticatorImpl).authenticateOnCASAndRetrieveCookies(hc, postRequest);
        doReturn(getRequest).when(casAuthenticatorImpl).createHttpRequestForTicketRetrieval(action, headers);
        HttpParams hp = mock(HttpParams.class);
        doReturn(hp).when(casAuthenticatorImpl).getHttpParams(hc);

        doReturn(response).when(casAuthenticatorImpl).executeTicketRequest(hc, getRequest);
        String ticket = "ST-sdgdfkjldkfjg";
        doReturn(ticket).when(casAuthenticatorImpl).retrieveTicketFromRedirect(response);
        StatusLine sl = mock(StatusLine.class);
        when(response.getStatusLine()).thenReturn(sl);
        when(sl.getStatusCode()).thenReturn(404);
        casAuthenticatorImpl.loginToServiceViaCAS(credentials);

        verify(casAuthenticatorImpl, times(1)).retrieveUsernameFromCredentials(credentials);
        verify(casAuthenticatorImpl, times(1)).retrievePasswordFromCredentials(credentials);
        verify(casAuthenticatorImpl, times(1)).createDefaultHttpClient();
        verify(casAuthenticatorImpl, times(1)).retrieveCASLoginPage(hc);
        verify(casAuthenticatorImpl, times(1)).createDOMDocumentFromResponse(content);
        verify(casAuthenticatorImpl, times(1)).extractLtInputFromDOMDocument(document);
        verify(casAuthenticatorImpl, times(1)).extractFormActionFromDOMDocument(document);

        verify(casAuthenticatorImpl, times(1)).createCasAuthenticationHttpRequest(lt, action, login, password);
        verify(casAuthenticatorImpl, times(1)).authenticateOnCASAndRetrieveCookies(hc, postRequest);
        verify(casAuthenticatorImpl, times(1)).createHttpRequestForTicketRetrieval(action, headers);
        verify(casAuthenticatorImpl, times(1)).getHttpParams(hc);

        verify(casAuthenticatorImpl, times(1)).executeTicketRequest(hc, getRequest);
        verify(casAuthenticatorImpl, never()).retrieveTicketFromRedirect(response);

        verify(response, times(2)).getStatusLine();
        verify(sl, times(1)).getStatusCode();
        assertThat(credentials).doesNotContainEntry("ticket", ticket);
    }

    @Test
    public void testLoginToServiceViaCASThrowError() throws Exception {
        String login = "username";
        String password = "password";
        doReturn(login).when(casAuthenticatorImpl).retrieveUsernameFromCredentials(credentials);
        doReturn(password).when(casAuthenticatorImpl).retrievePasswordFromCredentials(credentials);
        doReturn(hc).when(casAuthenticatorImpl).createDefaultHttpClient();
        String content = "some content";
        doReturn(content).when(casAuthenticatorImpl).retrieveCASLoginPage(hc);
        doReturn(document).when(casAuthenticatorImpl).createDOMDocumentFromResponse(content);
        String lt = "LT-sdfmlkjfg";
        doReturn(lt).when(casAuthenticatorImpl).extractLtInputFromDOMDocument(document);
        String action = "http://cas/actionSubmit";
        doReturn(action).when(casAuthenticatorImpl).extractFormActionFromDOMDocument(document);

        doReturn(postRequest).when(casAuthenticatorImpl).createCasAuthenticationHttpRequest(lt, action, login, password);
        Header[] headers = new Header[] { header };
        doReturn(headers).when(casAuthenticatorImpl).authenticateOnCASAndRetrieveCookies(hc, postRequest);
        doReturn(getRequest).when(casAuthenticatorImpl).createHttpRequestForTicketRetrieval(action, headers);
        HttpParams hp = mock(HttpParams.class);
        doReturn(hp).when(casAuthenticatorImpl).getHttpParams(hc);

        doThrow(new SocketTimeoutException()).when(casAuthenticatorImpl).executeTicketRequest(hc, getRequest);
        String ticket = "ST-sdgdfkjldkfjg";
        doReturn(ticket).when(casAuthenticatorImpl).retrieveTicketFromRedirect(response);
        StatusLine sl = mock(StatusLine.class);
        when(response.getStatusLine()).thenReturn(sl);
        when(sl.getStatusCode()).thenReturn(404);
        casAuthenticatorImpl.loginToServiceViaCAS(credentials);

        verify(casAuthenticatorImpl, times(1)).retrieveUsernameFromCredentials(credentials);
        verify(casAuthenticatorImpl, times(1)).retrievePasswordFromCredentials(credentials);
        verify(casAuthenticatorImpl, times(1)).createDefaultHttpClient();
        verify(casAuthenticatorImpl, times(1)).retrieveCASLoginPage(hc);
        verify(casAuthenticatorImpl, times(1)).createDOMDocumentFromResponse(content);
        verify(casAuthenticatorImpl, times(1)).extractLtInputFromDOMDocument(document);
        verify(casAuthenticatorImpl, times(1)).extractFormActionFromDOMDocument(document);

        verify(casAuthenticatorImpl, times(1)).createCasAuthenticationHttpRequest(lt, action, login, password);
        verify(casAuthenticatorImpl, times(1)).authenticateOnCASAndRetrieveCookies(hc, postRequest);
        verify(casAuthenticatorImpl, times(1)).createHttpRequestForTicketRetrieval(action, headers);
        verify(casAuthenticatorImpl, times(1)).getHttpParams(hc);

        verify(casAuthenticatorImpl, times(1)).executeTicketRequest(hc, getRequest);
        verify(casAuthenticatorImpl, never()).retrieveTicketFromRedirect(response);

        verify(response, never()).getStatusLine();
        verify(sl, never()).getStatusCode();
        assertThat(credentials).doesNotContainEntry("ticket", ticket);
    }

    @Test
    public void testCreateCasAuthenticationHttpRequest() throws Exception {
        String login = "username";
        String password = "password";
        String lt = "LT-sdfmlkjfg";
        String action = "actionSubmit";
        doReturn(new ArrayList<NameValuePair>()).when(casAuthenticatorImpl).createEntityContent(lt, login, password);
        HttpPost post = casAuthenticatorImpl.createCasAuthenticationHttpRequest(lt, action, login, password);
        assertThat(post.getURI()).isNotNull();
        assertThat(post.getURI().toASCIIString()).isNotNull().isEqualToIgnoringCase(casURLPRefix + action);
        verify(casAuthenticatorImpl, times(1)).createEntityContent(lt, login, password);
    }

    @Test
    public void testCreateDOMDocumentFromResponse() throws Exception {
        final String content = "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\">"
                + "<html><head><meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\" />"
                + "<title>TEST</title></head><body>Test de formatting!</body></html>";
        final Document doc = casAuthenticatorImpl.createDOMDocumentFromResponse(content);
        final String xmlDoc = transformDocumentToString(doc).replaceAll(LINE_SEPARATOR, "");
        // uncomment to see diff in eclipse
        // assertEquals(xmlDoc, "<html>  <head><META http-equiv=\"Content-Type\" "
        // + "content=\"text/html; charset=UTF-8\">    <meta content=\"text/html; charset=UTF-8\" http-equiv=\"Content-Type\">    "
        // + "<title>TEST</title>  </head>  <body>Test de formatting!</body></html>");

        assertThat(xmlDoc).isEqualToIgnoringCase("<html>  <head><META http-equiv=\"Content-Type\" "
                + "content=\"text/html; charset=UTF-8\">    <meta content=\"text/html; charset=UTF-8\" http-equiv=\"Content-Type\">    "
                + "<title>TEST</title>  </head>  <body>Test de formatting!</body></html>");
    }

    @Test
    public void testCreateDOMDocumentFromResponseWithLoginForm() throws Exception {
        final String content = FileUtils
                .readFileToString(new File("src/test/resources/com/bonita/engine/authentification/impl/cas/CAS-loginpage.html"), "UTF-8");
        final Document doc = casAuthenticatorImpl.createDOMDocumentFromResponse(content);
        final String xmlDoc = transformDocumentToString(doc).replaceAll(LINE_SEPARATOR, "");
        // uncomment to see diff in eclipse
        // assertEquals(sw.toString(), FileUtils.readFileToString(new
        // File("src/test/resources/com/bonita/engine/authentification/impl/cas/CAS-loginpage.xml")));
		final String readFileToString = FileUtils
                .readFileToString(new File("src/test/resources/com/bonita/engine/authentification/impl/cas/CAS-loginpage.xml"), "UTF-8")
                .replaceAll(REGEX_LINEBREAK_ALL_PLATFORMS, "");
        assertThat(xmlDoc).isEqualToIgnoringCase(readFileToString);
    }

    protected String transformDocumentToString(Document doc) throws TransformerFactoryConfigurationError, TransformerConfigurationException,
            TransformerException {
        final TransformerFactory tFactory = TransformerFactory.newInstance();
        final Transformer transformer = tFactory.newTransformer();

        final DOMSource source = new DOMSource(doc);
        final StringWriter sw = new StringWriter();
        final StreamResult result = new StreamResult(sw);
        transformer.transform(source, result);
        return sw.toString();
    }

    @Test
    public void testRetrieveCASLoginPage() throws Exception {
        String content = "<html></html>";
        HttpClient httpClient = mock(HttpClient.class);
        when(httpClient.execute(any(HttpGet.class))).thenReturn(response);
        HttpEntity entity = new ByteArrayEntity(content.getBytes(), ContentType.TEXT_HTML);
        when(response.getEntity()).thenReturn(entity);
        String contentRetrieved = casAuthenticatorImpl.retrieveCASLoginPage(httpClient);
        assertThat(contentRetrieved).isEqualTo(content);
        verify(httpClient, times(1)).execute(any(HttpGet.class));
        verify(response, times(1)).getEntity();
    }

    @Test
    public void testExtractFormActionFromDOMDocumentWithoutForm() throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder
                .parse(new ByteArrayInputStream(("<html><body><div>test</div></body></html>").getBytes("UTF8")));
        try {
            casAuthenticatorImpl.extractFormActionFromDOMDocument(document);
        } catch (LoginException e) {
            assertThat(e).hasMessage("No Action attribute found in CAS login Form");
            return;
        }
        fail();
    }

    @Test
    public void testExtractFormActionFromDOMDocument() throws Exception {
        String action = "cas/login";
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder
                .parse(new ByteArrayInputStream(("<html><body><form action='" + action + "'><div>test</div></form></body></html>").getBytes("UTF8")));
        String actionResult = casAuthenticatorImpl.extractFormActionFromDOMDocument(document);
        assertThat(actionResult).isEqualTo(action);
    }

    @Test
    public void testExtractLtInputFromDOMDocumentWithoutLTHiddenInput() throws Exception {
        String lt = "LT-sdmqljkgmdfk";
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder
                .parse(new ByteArrayInputStream(
                        ("<html><body><form action='cas/login'><div>test</div><div name='lt' value='" + lt + "'/></form></body></html>")
                                .getBytes("UTF8")));
        try {
            casAuthenticatorImpl.extractLtInputFromDOMDocument(document);
        } catch (LoginException e) {
            assertThat(e).hasMessage("No LT parameter found in CAS login Form");
            return;
        }
        fail();
    }

    @Test
    public void testExtractLtInputFromDOMDocument() throws Exception {
        String lt = "LT-sdmqljkgmdfk";
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder
                .parse(new ByteArrayInputStream(
                        ("<html><body><form action='cas/login'><div>test</div><input type='hidden' name='lt' value='" + lt + "'/></form></body></html>")
                                .getBytes("UTF8")));
        String ltResult = casAuthenticatorImpl.extractLtInputFromDOMDocument(document);
        assertThat(ltResult).isEqualTo(lt);
    }

    @Test
    public void testAuthenticateOnCASAndRetrieveCookies() throws Exception {
        String content = "<html></html>";
        HttpClient httpClient = mock(HttpClient.class);
        Header[] headers = new Header[] { header };
        when(response.getHeaders("Set-Cookie")).thenReturn(headers);
        when(httpClient.execute(any(HttpGet.class))).thenReturn(response);
        HttpEntity entity = new ByteArrayEntity(content.getBytes(), ContentType.TEXT_HTML);
        when(response.getEntity()).thenReturn(entity);
        Header[] headersResult = casAuthenticatorImpl.authenticateOnCASAndRetrieveCookies(httpClient, postRequest);
        assertThat(headersResult).contains(header);
        verify(httpClient, times(1)).execute(postRequest);
    }

    @Test
    public void testCreateHttpRequestForTicketRetrieval() {
        String casAction = "cas/login";
        String ticketGrantingCookie = "TGT-sdfkjfgsjfgsfg";
        Header[] headers = new Header[] { header };
        when(header.getValue()).thenReturn(ticketGrantingCookie);
        HttpGet get = casAuthenticatorImpl.createHttpRequestForTicketRetrieval(casAction, headers);
        assertThat(get.getURI().toASCIIString()).isEqualTo(casURLPRefix + casAction + "?service=http%3A%2F%2Fbonitasoft.com%2Fbonita");
        assertThat(get.getHeaders("Cookie")).isNotEmpty().hasSize(1);
        assertThat(get.getHeaders("Cookie")[0].getValue()).isEqualTo(ticketGrantingCookie);

    }

    @Test
    public void testRetrieveTicketFromRedirect() throws Exception {
        String ticket = "ST-sddflgsdfgkj";
        Header header = mock(Header.class);
        when(response.getFirstHeader("Location")).thenReturn(header);
        when(header.getValue()).thenReturn("http://cas.bonitasoft.com/bonita/portal?ticket=" + ticket);
        String ticketResult = casAuthenticatorImpl.retrieveTicketFromRedirect(response);
        assertThat(ticketResult).isEqualTo(ticket);
    }

    @Test
    public void testRetrieveTicketFromRedirectToSomethingElse() {
        String location = "http://cas.bonitasoft.com/bonita/portal?";
        Header header = mock(Header.class);
        when(response.getFirstHeader("Location")).thenReturn(header);
        when(header.getValue()).thenReturn(location);
        try {
            casAuthenticatorImpl.retrieveTicketFromRedirect(response);
        } catch (Exception e) {
            assertThat(e).hasMessage("impossible to retrieve ticket from CAS redirection..." + location);
            return;
        }
        fail();
    }

    @Test
    public void testCreateEntityContent() {
        String login = "username";
        String password = "password";
        String lt = "LT-sdfmlkjfg";
        List<NameValuePair> nameValuePairs = casAuthenticatorImpl.createEntityContent(lt, login, password);
        assertThat(nameValuePairs).contains(new BasicNameValuePair("username", login), new BasicNameValuePair("password", password),
                new BasicNameValuePair("lt", lt), new BasicNameValuePair("service", casService), new BasicNameValuePair("execution", "e1s1"),
                new BasicNameValuePair("_eventId", "submit"));
    }

}
