/**
 * Copyright (C) 2026 Bonitasoft S.A.
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
package org.bonitasoft.console.common.server.filter;

import static javax.servlet.http.HttpServletResponse.SC_UNAUTHORIZED;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.stream.Stream;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.catalina.Context;
import org.apache.catalina.startup.Tomcat;
import org.apache.tomcat.util.descriptor.web.FilterDef;
import org.apache.tomcat.util.descriptor.web.FilterMap;
import org.bonitasoft.console.common.server.login.filter.AuthenticationFilter;
import org.bonitasoft.console.common.server.login.filter.RestAPIAuthorizationFilter;
import org.bonitasoft.console.common.server.login.filter.TokenValidatorFilter;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Integration test verifying that path traversal attacks using semicolons (..;)
 * are blocked when running in an embedded Tomcat container (the production container).
 * <p>
 * Each filter stub extends the real filter class, inheriting the actual
 * {@code doFilter()} &rarr; {@code matchExcludePatterns()} &rarr;
 * {@code URLExcludePattern} + {@code PathSanitizer} code path.
 * Only {@code proceedWithFiltering()} is overridden to short-circuit engine dependencies.
 * <p>
 * This tests the CVE-233 fix end-to-end:
 * <ul>
 * <li>Tomcat's URL parsing and semicolon handling</li>
 * <li>Filter chain execution with REQUEST and FORWARD dispatchers</li>
 * <li>PathSanitizer-based defense in URLExcludePattern</li>
 * </ul>
 */
public class PathTraversalProtectionIT {

    private static final int HTTP_TIMEOUT_MS = 5000;

    private static Tomcat tomcat;
    private static File baseDir;
    private static int port;

    @BeforeClass
    public static void startTomcat() throws Exception {
        tomcat = new Tomcat();
        tomcat.setPort(0);

        baseDir = new File(System.getProperty("java.io.tmpdir"), "tomcat-it-" + System.nanoTime());
        tomcat.setBaseDir(baseDir.getAbsolutePath());

        Context context = tomcat.addContext("/bonita", null);

        // Register filters in the same order as web.xml
        addFilter(context, "TokenValidatorFilter", StubTokenValidatorFilter.class,
                "/apps/*", "REQUEST", "FORWARD");
        addFilter(context, "AuthenticationFilter", StubAuthenticationFilter.class,
                "/apps/*", "REQUEST", "FORWARD");
        addFilter(context, "RestAPIAuthorizationFilter", StubRestAPIAuthorizationFilter.class,
                "/API/*", "REQUEST", "FORWARD");

        // Register servlets
        Tomcat.addServlet(context, "apps", new StubOkServlet());
        context.addServletMappingDecoded("/apps/*", "apps");

        Tomcat.addServlet(context, "serverAPI", new StubForbiddenServlet());
        context.addServletMappingDecoded("/serverAPI/*", "serverAPI");

        tomcat.start();
        port = tomcat.getConnector().getLocalPort();
    }

    @AfterClass
    public static void stopTomcat() throws Exception {
        if (tomcat != null) {
            tomcat.stop();
            tomcat.destroy();
        }
        if (baseDir != null && baseDir.exists()) {
            try (Stream<Path> paths = Files.walk(baseDir.toPath())) {
                paths.sorted(Comparator.reverseOrder())
                        .map(Path::toFile)
                        .forEach(File::delete);
            }
        }
    }

    /**
     * Semicolon traversal that stays within the /apps/* context after Tomcat normalization.
     * Tomcat strips path parameters and normalizes '..' segments, routing the request to
     * /apps/serverAPI/test. The filter chain runs and blocks the request (401).
     */
    @Test
    public void semicolonTraversalStayingInContextShouldBeBlocked() throws Exception {
        // /apps/A/B/../../serverAPI/test normalizes to /apps/serverAPI/test (within /apps/*)
        int status = get("/bonita/apps/A/B/..;/..;/serverAPI/test");

        assertThat(status).as("Semicolon traversal staying within /apps/* context must be blocked")
                .isNotEqualTo(200);
    }

    /**
     * Percent-encoded semicolons (%3b) are NOT decoded by Tomcat during routing, so
     * the filter receives the raw URL. URLExcludePattern URL-decodes then strips path
     * parameters via PathSanitizer, correctly detecting the traversal.
     */
    @Test
    public void percentEncodedSemicolonTraversalShouldBeBlocked() throws Exception {
        int status = get("/bonita/apps/..%3b/..%3b/serverAPI/test");

        assertThat(status).as("Percent-encoded semicolon traversal must not succeed")
                .isNotEqualTo(200);
    }

    /**
     * Deep traversal that exploits the filter exclude pattern. The URL is crafted so
     * that the raw path matches an exclude pattern (e.g. apps/.+/API/system/session),
     * but after PathSanitizer strips semicolons and normalizes '..', the resolved path
     * no longer matches the exclude pattern.
     */
    @Test
    public void deepTraversalViaExcludePatternShouldBeBlocked() throws Exception {
        int status = get("/bonita/apps/FAKE/API/system/session/..;/..;/..;/..;/serverAPI/x");

        assertThat(status).as("Deep traversal exploiting exclude pattern must not succeed")
                .isNotEqualTo(200);
    }

    @Test
    public void normalAppRequestShouldRequireAuthentication() throws Exception {
        int status = get("/bonita/apps/myapp");

        assertThat(status).as("Normal app request should be blocked by auth filter")
                .isEqualTo(SC_UNAUTHORIZED);
    }

    @Test
    public void urlWithEncodedSpacesShouldPassThrough() throws Exception {
        int status = get("/bonita/apps/My%20App/API/system/i18ntranslation");

        assertThat(status).as("URL with encoded spaces in an excluded path should reach the servlet")
                .isEqualTo(200);
    }

    @Test
    public void legitimateExcludedUrlShouldPassThrough() throws Exception {
        int status = get("/bonita/apps/myapp/API/system/i18ntranslation");

        assertThat(status).as("Legitimate excluded URL should reach the servlet")
                .isEqualTo(200);
    }

    // --- Helper methods ---

    private static int get(String path) throws IOException {
        URL url = new URL("http://localhost:" + port + path);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setInstanceFollowRedirects(false);
        conn.setConnectTimeout(HTTP_TIMEOUT_MS);
        conn.setReadTimeout(HTTP_TIMEOUT_MS);
        try {
            return conn.getResponseCode();
        } finally {
            conn.disconnect();
        }
    }

    private static void addFilter(Context ctx, String name,
            Class<? extends Filter> clazz, String urlPattern,
            String... dispatchers) {
        FilterDef filterDef = new FilterDef();
        filterDef.setFilterName(name);
        filterDef.setFilterClass(clazz.getName());
        ctx.addFilterDef(filterDef);

        FilterMap filterMap = new FilterMap();
        filterMap.setFilterName(name);
        filterMap.addURLPattern(urlPattern);
        for (String d : dispatchers) {
            filterMap.setDispatcher(d);
        }
        ctx.addFilterMap(filterMap);
    }

    // --- Stub filters (public static for Tomcat reflection) ---

    public static class StubTokenValidatorFilter extends TokenValidatorFilter {

        @Override
        public void proceedWithFiltering(ServletRequest request, ServletResponse response,
                FilterChain chain) throws ServletException, IOException {
            ((HttpServletResponse) response).setStatus(SC_UNAUTHORIZED);
        }
    }

    public static class StubAuthenticationFilter extends AuthenticationFilter {

        @Override
        public void proceedWithFiltering(ServletRequest request, ServletResponse response,
                FilterChain chain) throws ServletException, IOException {
            ((HttpServletResponse) response).setStatus(SC_UNAUTHORIZED);
        }
    }

    public static class StubRestAPIAuthorizationFilter extends RestAPIAuthorizationFilter {

        @Override
        public void proceedWithFiltering(ServletRequest request, ServletResponse response,
                FilterChain chain) throws ServletException {
            ((HttpServletResponse) response).setStatus(SC_UNAUTHORIZED);
        }
    }

    // --- Stub servlets ---

    public static class StubOkServlet extends HttpServlet {

        @Override
        protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
            resp.setStatus(200);
            resp.getWriter().write("OK");
        }
    }

    public static class StubForbiddenServlet extends HttpServlet {

        @Override
        protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
            resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
            resp.getWriter().write("FORBIDDEN");
        }
    }
}
