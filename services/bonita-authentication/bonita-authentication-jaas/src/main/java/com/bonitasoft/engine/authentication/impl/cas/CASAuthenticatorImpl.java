/**
 * Copyright (C) 2013 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2.0 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
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
 * 
 */
public class CASAuthenticatorImpl implements AuthenticatorDelegate {

    protected final TechnicalLoggerService logger;

    protected String casServerUrlPrefix;

    protected String casService;

    /**
     * Default Constructor.
     */
    public CASAuthenticatorImpl(TechnicalLoggerService logger) {
        this.logger = logger;
    }

    /**
     * @see com.bonitasoft.engine.authentication.impl.AuthenticatorDelegate#authenticate(java.util.Map)
     */
    @Override
    public Map<String, Serializable> authenticate(Map<String, Serializable> credentials) {
        return loginToServiceViaCAS(credentials);
    }

    /**
     * login to a service with a CAS authentication
     */
    protected Map<String, Serializable> loginToServiceViaCAS(Map<String, Serializable> credentials) {
        try {
            String username = retrieveUsernameFromCredentials(credentials);
            String password = retrievePasswordFromCredentials(credentials);
            DefaultHttpClient hc = createDefaultHttpClient();
            String content = retrieveCASLoginPage(hc);
            Document document = createDOMDocumentFromResponse(content);
            String lt = extractLtInputFromDOMDocument(document);
            String action = extractFormActionFromDOMDocument(document);
            HttpPost postRequest = createCasAuthenticationHttpRequest(lt, action, username, password);
            Header[] headers = authenticateOnCASAndRetrieveCookies(hc, postRequest);
            HttpGet getRequest = createHttpRequestForTicketRetrieval(action, headers);

            HttpClientParams.setRedirecting(getHttpParams(hc), false);
            HttpResponse response = executeTicketRequest(hc, getRequest);
            if (response != null && response.getStatusLine() != null && response.getStatusLine().getStatusCode() == 302) {
                Map<String, Serializable> credentialResult = new HashMap<String, Serializable>();
                credentialResult.put(AuthenticationConstants.CAS_TICKET, retrieveTicketFromRedirect(response));
                credentialResult.put(AuthenticationConstants.BASIC_TENANT_ID, credentials.get(AuthenticationConstants.BASIC_TENANT_ID));
                return credentialResult;
            } else {
                logger.log(CASAuthenticatorImpl.class, TechnicalLogSeverity.WARNING, "impossible to login");
            }
        } catch (Exception e) {
            logger.log(CASAuthenticatorImpl.class, TechnicalLogSeverity.WARNING, "impossible to login due to following exception : ", e);
        }
        return credentials;

    }

    /**
     * @param hc
     * @return
     */
    protected HttpParams getHttpParams(DefaultHttpClient hc) {
        return hc.getParams();
    }

    /**
     * @param hc
     * @param getRequest
     * @return
     * @throws IOException
     * @throws ClientProtocolException
     */
    protected HttpResponse executeTicketRequest(HttpClient hc, HttpGet getRequest) throws IOException, ClientProtocolException {
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
     *            the credentials to check
     * @return the password
     * @throws SLoginException
     *             if password is absent or if credentials is null
     */
    protected String retrievePasswordFromCredentials(Map<String, Serializable> credentials) throws LoginException {
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
     *            the credentials to check
     * @return the username
     * @throws SLoginException
     *             if username is absent, blank or if credentials is null
     */
    protected String retrieveUsernameFromCredentials(Map<String, Serializable> credentials) throws LoginException {
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
     *            the http response
     * @throws LoginException
     *             if ticket cannot be retrieved
     */
    protected String retrieveTicketFromRedirect(HttpResponse response) throws LoginException {
        Header h = response.getFirstHeader("Location");
        if (logger.isLoggable(CASAuthenticatorImpl.class, TechnicalLogSeverity.DEBUG)) {
            logger.log(CASAuthenticatorImpl.class, TechnicalLogSeverity.DEBUG, "redirection vers : " + h.getValue());
        }
        Pattern pattern = Pattern.compile("ticket=([^\\&]*)(\\&|$)");
        Matcher m = pattern.matcher(h.getValue());
        if (m.find()) {
            return m.group(1);
        } else {
            throw new LoginException("impossible to retrieve ticket from CAS redirection..." + h.getValue());
        }
    }

    /**
     * create a http request to request for credential for a given service (see CAS protocol for further information).
     * takes the Ticket Granting Cookie as input parameter as well as the CAS server url
     * 
     * @param action
     *            the cas server URL
     * @param headers
     *            the cookies to set on the request
     * @return the http request
     */
    protected HttpGet createHttpRequestForTicketRetrieval(String action, Header[] headers) {
        List<NameValuePair> nvps = new ArrayList<NameValuePair>();
        nvps.add(new BasicNameValuePair("service", casService));
        String params = URLEncodedUtils.format(nvps, "UTF8");
        HttpGet getRequest = new HttpGet(casServerUrlPrefix + action + "?" + params);

        for (Header header : headers) {
            getRequest.addHeader("Cookie", header.getValue());
        }
        return getRequest;
    }

    /**
     * call the given post Request and retrieve the Set-Cookie headers of the http response the server gave us
     * 
     * these are useful to manage the Ticket Granting Cookie of CAS protocol
     * 
     * @param postRequest
     *            the request to send
     * @return the Set-Cookie headers
     */
    protected Header[] authenticateOnCASAndRetrieveCookies(HttpClient hc, HttpPost postRequest) throws IOException, ClientProtocolException {
        HttpResponse response;
        response = hc.execute(postRequest);
        EntityUtils.consumeQuietly(response.getEntity());
        Header[] headers = response.getHeaders("Set-Cookie");
        return headers;
    }

    /**
     * retrieve the lt security parameter contained in a hidden input of the login form
     * 
     * @return the lt security parameter value
     * @throws XPathExpressionException
     * @throws LoginException
     *             if no LT parameter has been found
     */
    protected String extractLtInputFromDOMDocument(Document document) throws XPathExpressionException, LoginException {
        javax.xml.xpath.XPathFactory xpathFactory = javax.xml.xpath.XPathFactory.newInstance();
        XPath xpath = xpathFactory.newXPath();
        String result = xpath.evaluate("//input[@name='lt']/@value", document
                .getDocumentElement());
        if (StringUtils.isNotBlank(result)) {
            return result;
        } else {
            throw new LoginException("No LT parameter found in CAS login Form");
        }
    }

    /**
     * extract the form action of the given DOM Document
     * 
     * @return the action to submit the for to
     * @throws LoginException
     *             if No Action attribute has been found
     */
    protected String extractFormActionFromDOMDocument(Document document) throws XPathExpressionException, LoginException {
        javax.xml.xpath.XPathFactory xpathFactory = javax.xml.xpath.XPathFactory.newInstance();
        XPath xpath = xpathFactory.newXPath();
        String result = xpath.evaluate("//form/@action", document
                .getDocumentElement());
        if (StringUtils.isNotBlank(result)) {
            return result;
        } else {
            throw new LoginException("No Action attribute found in CAS login Form");
        }

    }

    /**
     * create CAS authentication request for login form submit
     * 
     * @param lt
     *            a security parameter contained in the login form
     * @param action
     *            the form action to submit to
     * @return the request to send to an HttpClient
     */
    protected HttpPost createCasAuthenticationHttpRequest(String lt, String action, String login, String password) throws UnsupportedEncodingException {
        List<NameValuePair> nvps = createEntityContent(lt, login, password);

        HttpPost postRequest = new HttpPost(casServerUrlPrefix + action);
        postRequest.setEntity(new UrlEncodedFormEntity(nvps, "utf8"));
        return postRequest;
    }

    /**
     * creates the list of http parameter to authenticate to CAS Server
     * 
     * @param lt
     *            the lt securty token
     * @param login
     *            the CAS login
     * @param password
     *            the CAS password associated to the login
     * @return the list of http parameter as name/value pair
     */
    protected List<NameValuePair> createEntityContent(String lt, String login, String password) {
        List<NameValuePair> nvps = new ArrayList<NameValuePair>();
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
     *            the page to transform
     * @return a DOM document object for further process
     */
    protected Document createDOMDocumentFromResponse(String content) throws UnsupportedEncodingException, ParserConfigurationException, SAXException,
            IOException {
        Tidy tidy = new Tidy();
        tidy.setQuiet(true);
        tidy.setTidyMark(false);
        tidy.setMakeClean(true);
        tidy.setXmlOut(true);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        tidy.parse(new ByteArrayInputStream(content.getBytes("UTF8")), byteArrayOutputStream);
        content = new String(byteArrayOutputStream.toByteArray(), "UTF8");

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();

        builder.setEntityResolver(new EntityResolver() {

            @Override
            public InputSource resolveEntity(String publicId, String systemId)
                    throws SAXException, IOException {
                return new InputSource(new StringReader(""));
            }
        });
        Document document = builder.parse(new ByteArrayInputStream(content.getBytes("UTF8")));
        return document;
    }

    /**
     * call CAS server login page via HTTP
     * 
     * @return the result of the http call
     */
    protected String retrieveCASLoginPage(HttpClient hc) throws IOException, ClientProtocolException {
        HttpUriRequest request = new HttpGet(getCasServerUrl());
        HttpResponse response = hc.execute(request);
        String content = EntityUtils.toString(response.getEntity());
        return content;
    }

    /**
     * @return the casURLPRefix
     */
    public String getCasServerUrlPrefix() {
        return casServerUrlPrefix;
    }

    /**
     * @param casURLPRefix
     *            the casURLPRefix to set
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
     *            the casService to set
     */
    public void setCasService(String casService) {
        this.casService = casService;
    }
}
