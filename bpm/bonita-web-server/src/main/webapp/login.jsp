<%-- Copyright (C) 2009 BonitaSoft S.A.
 BonitaSoft, 31 rue Gustave Eiffel - 38000 Grenoble
 This program is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 2.0 of the License, or
 (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program.  If not, see <http://www.gnu.org/licenses/>. --%>
<%@page language="java"%>
<%@page contentType="text/html; charset=UTF-8"%>
<%@page import="java.net.URLEncoder"%>
<%@page import="org.apache.commons.lang3.StringEscapeUtils"%>
<%@page import="org.bonitasoft.console.common.server.jsp.JSPUtils"%>
<%@page import="org.bonitasoft.console.common.server.jsp.JSPI18n"%>
<%
    JSPUtils JSP = new JSPUtils(request, session);
    JSPI18n i18n = new JSPI18n(JSP);

    // Build Action URL
    String redirectUrl = JSP.getParameter("redirectUrl");

    StringBuffer actionUrl = new StringBuffer("loginservice?redirect=true");
    StringBuffer styleUrl = new StringBuffer("portal-theme");

    if (redirectUrl != null) {
        actionUrl.append("&redirectUrl=" + URLEncoder.encode(redirectUrl, "UTF-8"));
    }

    // Error messages
    String errorMessage = "";
    boolean disableLogin = false;
    String noBonitaHomeMessage = request.getAttribute("noBonitaHomeMessage") + "";
	String noBonitaClientFileMessage = request.getAttribute("noBonitaClientFileMessage") + "";
	String loginFailMessage = request.getAttribute("loginFailMessage") + "";

    // Technical problems
    if (
        !JSP.getParameter("isPlatformCreated", true) ||
		!JSP.getParameter("isTenantCreated", true) ||
		"tenantNotActivated".equals(loginFailMessage) ||
		"noBonitaHomeMessage".equals(noBonitaHomeMessage) ||
		"noBonitaClientFileMessage".equals(noBonitaClientFileMessage)
	) {
        errorMessage = i18n.t_("The server is not available") + "<br />" + i18n.t_("Please, contact your administrator.");
        disableLogin = true;
    }
    // No profile for this user
    else if ("noProfileForUser".equals(loginFailMessage)) {
        errorMessage = i18n.t_("Login failed. No profile has been set up for this user. Contact your administrator.");
    }
 	// Login or password error
    else if ("loginFailMessage".equals(loginFailMessage)) {
        errorMessage = i18n.t_("Unable to log in. Please check your username and password.");
    }
%>
<!DOCTYPE html>
<html>
<head>
<meta name="viewport" content="width=device-width, user-scalable=no">
<meta http-equiv="content-type" content="text/html; charset=UTF-8" />
<title>Bonita Applications</title>
<link rel="icon" type="image/png" href="<%= styleUrl %>/images/favicon2.ico" />
<link rel="stylesheet" type="text/css" href="<%= styleUrl %>/css/login.css"/>

<script>
  document.addEventListener("DOMContentLoaded", function() {
		if (window != window.top) {
			try {
				if (window.frameElement.id == "bonitaframe") {
					/* if the login jsp is displayed inside a "bonitaframe" iframe it probably means the session is invalid so refresh the whole page */
					window.parent.location.reload();
					return;
				}
			} catch (e) {
				/* nothing to do (bonita is probably displayed inside an iframe of a different domain app) */
			}
		}
		/* Add url hash to form action url */
		var form = document.getElementById('LoginForm');
		form.setAttribute('action', form.getAttribute('action') + window.location.hash);
	});
</script>

</head>
<body id="LoginPage">

	<div id="LoginHeader">
		<h1><span><%= i18n.t_("Welcome to") %></span> <%= i18n.t_("Bonita Applications") %></h1>
	</div>

	<div id="floater"></div>

	<div class="LoginFormWrapper">
	    <div id="LoginFormContainer" >
		<div id="logo">
			<img src="<%= styleUrl %>/skin/images/login-logo.png"/>
		</div>

		<div class="body">
			<form id="LoginForm" action="<%=actionUrl%>" method="post" autocomplete="off">

				<div class="header">
					<h2><%=i18n.t_("Login form")%></h2>
				</div>

				<p class="error"><%=errorMessage.length() > 0 ? errorMessage  : ""%></p>

				<div class="formentries">

					<div class="formentry">
						<div class="label">
							<label for="username"><%=i18n.t_("User")%></label>
						</div>
						<div class="input">
							<input id="username"
                                   name="username"
                                   value="<%= StringEscapeUtils.escapeHtml4(JSP.getSessionOrCookie("username", "")) %>"
                                   placeholder="<%=i18n.t_("User")%>"
                                   type="text"
                                   autocomplete="off"
                                   tabindex="1"
                                   maxlength="255" <%=disableLogin ? "disabled=\"disabled\" " : ""%>
                            />
						</div>
					</div>

					<div class="formentry">
						<div class="label">
							<label for="password"><%=i18n.t_("Password")%></label>
						</div>
						<div class="input">
							<input id="password"
                                   name="password"
                                   type="password"
                                   tabindex="2"
                                   autocomplete="off"
                                   maxlength="50"
                                   placeholder="<%=i18n.t_("Password")%>" <%=disableLogin ? "disabled=\"disabled\" " : ""%>
                            />
						</div>
						<input name="_l" type="hidden" value="<%=i18n.getLocale()%>" />
					</div>

				</div>
				<div class="formactions">
					<input type="submit"
                           value="<%=i18n.t_("Login")%>" <%=disableLogin ? "disabled=\"disabled\" " : ""%>
                    />
				</div>
			</form>
		</div>
	</div>
	</div>
</body>
</html>
