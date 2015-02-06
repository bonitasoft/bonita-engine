/*******************************************************************************
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.authentication.impl.cas;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.security.auth.login.LoginException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.params.HttpClientParams;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;
import org.bonitasoft.engine.authentication.AuthenticationConstants;
import org.bonitasoft.engine.log.technical.TechnicalLogSeverity;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.w3c.dom.Document;
import org.w3c.tidy.Tidy;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.bonitasoft.engine.authentication.impl.AuthenticatorDelegate;

/**
 * delegate the CAS authentication to a CAS Server
 * 
 * @author Julien Reboul
 */
public class CASAuthenticatorImpl implements AuthenticatorDelegate {

    protected final TechnicalLoggerService logger;

    protected String casServerUrlPrefix;

    protected String casService;

    protected CASUtils casUtils = CASUtils.getInstance();

    /**
     * Default Constructor.
     * 
     * @param logger
     */
    public CASAuthenticatorImpl(final TechnicalLoggerService logger) {
        this.logger = logger;
    }

    /**
     * @see com.bonitasoft.engine.authentication.impl.AuthenticatorDelegate#authenticate(java.util.Map)
     */
    @Override
    public Map<String, Serializable> authenticate(final Map<String, Serializable> credentials) {
        casUtils.checkLicense();
        return loginToServiceViaCAS(credentials);
    }

    /**
     * login to a service with a CAS authentication
     */
    protected Map<String, Serializable> loginToServiceViaCAS(final Map<String, Serializable> credentials) {
        String username;
        String password;
        try {
            username = retrieveUsernameFromCredentials(credentials);
            password = retrievePasswordFromCredentials(credentials);
        } catch (LoginException e) {
            logger.log(CASAuthenticatorImpl.class, TechnicalLogSeverity.WARNING,
                    "impossible to attempt to login to {casServerUrlPrefix:" + this.getCasServerUrl() + " | casServiceUrl:" + this.getCasService()
                            + "} due to following exception : " + e);
            return credentials;
        }
        try {
            final DefaultHttpClient hc = createDefaultHttpClient();
            final String content = retrieveCASLoginPage(hc);
            final Document document = createDOMDocumentFromResponse(content);
            final String lt = extractLtInputFromDOMDocument(document);
            final String action = extractFormActionFromDOMDocument(document);
            final HttpPost postRequest = createCasAuthenticationHttpRequest(lt, action, username, password);
            final Header[] headers = authenticateOnCASAndRetrieveCookies(hc, postRequest);
            final HttpGet getRequest = createHttpRequestForTicketRetrieval(action, headers);

            HttpClientParams.setRedirecting(getHttpParams(hc), false);
            final HttpResponse response = executeTicketRequest(hc, getRequest);
            if (response != null && response.getStatusLine() != null && response.getStatusLine().getStatusCode() == 302) {
                Map<String, Serializable> credentialResult = new HashMap<String, Serializable>();
                credentialResult.put(AuthenticationConstants.CAS_TICKET, retrieveTicketFromRedirect(response));
                credentialResult.put(AuthenticationConstants.BASIC_TENANT_ID, credentials.get(AuthenticationConstants.BASIC_TENANT_ID));
                return credentialResult;
            }
            logger.log(CASAuthenticatorImpl.class, TechnicalLogSeverity.WARNING,
                    "impossible to login to {casServerUrlPrefix:" + this.getCasServerUrl() + " | casServiceUrl:" + this.getCasService() + "}");
        } catch (Exception e) {
            logger.log(CASAuthenticatorImpl.class, TechnicalLogSeverity.WARNING,
                    "impossible to login to {casServerUrlPrefix:" + this.getCasServerUrl() + " | casServiceUrl:" + this.getCasService()
                            + "} due to following exception : ", e);
        }
        return credentials;

    }

    /**
     * @param hc
     * @return
     */
    protected HttpParams getHttpParams(final DefaultHttpClient hc) {
        return hc.getParams();
    }

    /**
     * @param hc
     * @param getRequest
     * @return
     * @throws IOException
     * @throws ClientProtocolException
     */
    protected HttpResponse executeTicketRequest(final HttpClient hc, final HttpGet getRequest) throws IOException, ClientProtocolException {
        return hc.execute(getRequest);
    }

    /**
     * for unit testing purpose
     * 
     * @return
     */
    protected DefaultHttpClient createDefaultHttpClient() {
        return new DefaultHttpClient();
    }

    /**
     * retrieve password from credentials assuming it is stored under the {@link AuthenticationConstants.BASIC_PASSWORD} key
     * 
     * @param credentials
     *        the credentials to check
     * @return the password
     * @throws SLoginException
     *         if password is absent or if credentials is null
     */
    protected String retrievePasswordFromCredentials(final Map<String, Serializable> credentials) throws LoginException {
        String password;
        if (credentials == null || !credentials.containsKey(AuthenticationConstants.BASIC_PASSWORD)
                || credentials.get(AuthenticationConstants.BASIC_PASSWORD) == null) {
            throw new LoginException("invalid credentials, password is absent");
        }
        password = String.valueOf(credentials.get(AuthenticationConstants.BASIC_PASSWORD));
        return password;
    }

    /**
     * retrieve username from credentials assuming it is stored under the {@link AuthenticationConstants.BASIC_USERNAME} key
     * 
     * @param credentials
     *        the credentials to check
     * @return the username
     * @throws SLoginException
     *         if username is absent, blank or if credentials is null
     */
    protected String retrieveUsernameFromCredentials(final Map<String, Serializable> credentials) throws LoginException {
        String userName;
        if (credentials == null || !credentials.containsKey(AuthenticationConstants.BASIC_USERNAME)
                || credentials.get(AuthenticationConstants.BASIC_USERNAME) == null
                || StringUtils.isBlank((userName = String.valueOf(credentials.get(AuthenticationConstants.BASIC_USERNAME))))) {
            throw new LoginException("invalid credentials, username is blank");
        }
        return userName;
    }

    /**
     * extract the ticket from the given response which should be a Http 302 status code
     * 
     * @param response
     *        the http response
     * @throws LoginException
     *         if ticket cannot be retrieved
     */
    protected String retrieveTicketFromRedirect(final HttpResponse response) throws LoginException {
        final Header h = response.getFirstHeader("Location");
        if (logger.isLoggable(CASAuthenticatorImpl.class, TechnicalLogSeverity.DEBUG)) {
            logger.log(CASAuthenticatorImpl.class, TechnicalLogSeverity.DEBUG, "redirection vers : " + h.getValue());
        }
        final Pattern pattern = Pattern.compile("ticket=([^\\&]*)(\\&|$)");
        final Matcher m = pattern.matcher(h.getValue());
        if (m.find()) {
            return m.group(1);
        }
        throw new LoginException("impossible to retrieve ticket from CAS redirection..." + h.getValue());
    }

    /**
     * create a http request to request for credential for a given service (see CAS protocol for further information).
     * takes the Ticket Granting Cookie as input parameter as well as the CAS server url
     * 
     * @param action
     *        the cas server URL
     * @param headers
     *        the cookies to set on the request
     * @return the http request
     */
    protected HttpGet createHttpRequestForTicketRetrieval(final String action, Header[] headers) {
        final List<NameValuePair> nvps = new ArrayList<NameValuePair>();
        nvps.add(new BasicNameValuePair("service", casService));
        final String params = URLEncodedUtils.format(nvps, "UTF8");
        final HttpGet getRequest = new HttpGet(casServerUrlPrefix + action + "?" + params);

        for (final Header header : headers) {
            getRequest.addHeader("Cookie", header.getValue());
        }
        return getRequest;
    }

    /**
     * call the given post Request and retrieve the Set-Cookie headers of the http response the server gave us
     * these are useful to manage the Ticket Granting Cookie of CAS protocol
     * 
     * @param postRequest
     *        the request to send
     * @return the Set-Cookie headers
     */
    protected Header[] authenticateOnCASAndRetrieveCookies(final HttpClient hc, final HttpPost postRequest) throws IOException, ClientProtocolException {
        final HttpResponse response = hc.execute(postRequest);
        EntityUtils.consumeQuietly(response.getEntity());
        return response.getHeaders("Set-Cookie");
    }

    /**
     * retrieve the lt security parameter contained in a hidden input of the login form
     * 
     * @return the lt security parameter value
     * @throws XPathExpressionException
     * @throws LoginException
     *         if no LT parameter has been found
     */
    protected String extractLtInputFromDOMDocument(final Document document) throws XPathExpressionException, LoginException {
        javax.xml.xpath.XPathFactory xpathFactory = javax.xml.xpath.XPathFactory.newInstance();
        final XPath xpath = xpathFactory.newXPath();
        final String result = xpath.evaluate("//input[@name='lt']/@value", document
                .getDocumentElement());
        if (StringUtils.isNotBlank(result)) {
            return result;
        }
        throw new LoginException("No LT parameter found in CAS login Form");
    }

    /**
     * extract the form action of the given DOM Document
     * 
     * @return the action to submit the for to
     * @throws LoginException
     *         if No Action attribute has been found
     */
    protected String extractFormActionFromDOMDocument(final Document document) throws XPathExpressionException, LoginException {
        javax.xml.xpath.XPathFactory xpathFactory = javax.xml.xpath.XPathFactory.newInstance();
        final XPath xpath = xpathFactory.newXPath();
        final String result = xpath.evaluate("//form/@action", document
                .getDocumentElement());
        if (StringUtils.isNotBlank(result)) {
            return result;
        }
        throw new LoginException("No Action attribute found in CAS login Form");

    }

    /**
     * create CAS authentication request for login form submit
     * 
     * @param lt
     *        a security parameter contained in the login form
     * @param action
     *        the form action to submit to
     * @return the request to send to an HttpClient
     */
    protected HttpPost createCasAuthenticationHttpRequest(final String lt, final String action, final String login, final String password)
            throws UnsupportedEncodingException {
        final List<NameValuePair> nvps = createEntityContent(lt, login, password);

        final HttpPost postRequest = new HttpPost(casServerUrlPrefix + action);
        postRequest.setEntity(new UrlEncodedFormEntity(nvps, "utf8"));
        return postRequest;
    }

    /**
     * creates the list of http parameter to authenticate to CAS Server
     * 
     * @param lt
     *        the lt securty token
     * @param login
     *        the CAS login
     * @param password
     *        the CAS password associated to the login
     * @return the list of http parameter as name/value pair
     */
    protected List<NameValuePair> createEntityContent(final String lt, final String login, final String password) {
        final List<NameValuePair> nvps = new ArrayList<NameValuePair>();
        nvps.add(new BasicNameValuePair("username", login));
        nvps.add(new BasicNameValuePair("password", password));
        nvps.add(new BasicNameValuePair("lt", lt));
        nvps.add(new BasicNameValuePair("service", casService));
        nvps.add(new BasicNameValuePair("execution", "e1s1"));
        nvps.add(new BasicNameValuePair("_eventId", "submit"));
        return nvps;
    }

    /**
     * create a DOM document from a String that should be at least an HTML page
     * 
     * @param content
     *        the page to transform
     * @return a DOM document object for further process
     */
    protected Document createDOMDocumentFromResponse(final String content) throws UnsupportedEncodingException, ParserConfigurationException, SAXException,
            IOException {
        final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        final DocumentBuilder builder = factory.newDocumentBuilder();
        builder.setEntityResolver(new EntityResolver() {

            @Override
            public InputSource resolveEntity(final String publicId, final String systemId) {
                return new InputSource(new StringReader(""));
            }
        });

        final Tidy tidy = new Tidy();
        tidy.setQuiet(true);
        tidy.setTidyMark(false);
        tidy.setMakeClean(true);
        tidy.setXmlOut(true);
        final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        tidy.parse(new ByteArrayInputStream(content.getBytes("UTF8")), byteArrayOutputStream);
        final String contentUTF8 = new String(byteArrayOutputStream.toByteArray(), "UTF8");
        return builder.parse(new ByteArrayInputStream(contentUTF8.getBytes("UTF8")));
    }

    /**
     * call CAS server login page via HTTP
     * 
     * @return the result of the http call
     */
    protected String retrieveCASLoginPage(final HttpClient hc) throws IOException, ClientProtocolException {
        final HttpUriRequest request = new HttpGet(getCasServerUrl());
        final HttpResponse response = hc.execute(request);
        return EntityUtils.toString(response.getEntity());
    }

    /**
     * @return the casURLPRefix
     */
    public String getCasServerUrlPrefix() {
        return casServerUrlPrefix;
    }

    /**
     * @param casURLPRefix
     *        the casURLPRefix to set
     */
    public void setCasServerUrlPrefix(String casURLPRefix) {
        this.casServerUrlPrefix = casURLPRefix;
    }

    /**
     * @return the casServerUrl
     */
    public String getCasServerUrl() {
        return casServerUrlPrefix + "/cas";
    }

    /**
     * @return the casService
     */
    public String getCasService() {
        return casService;
    }

    /**
     * @param casService
     *        the casService to set
     */
    public void setCasService(String casService) {
        this.casService = casService;
    }
}
