package org.bonitasoft.test.page

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import javax.servlet.http.HttpSession

import org.bonitasoft.engine.api.IdentityAPI
import org.bonitasoft.engine.api.TenantAPIAccessor
import org.bonitasoft.engine.identity.User
import org.bonitasoft.web.extension.page.PageContext
import org.bonitasoft.web.extension.page.PageController
import org.bonitasoft.web.extension.page.PageResourceProvider

class Index implements PageController {

    @Override
    void doGet(HttpServletRequest request, HttpServletResponse response, PageResourceProvider pageResourceProvider, PageContext pageContext) {
        try {
            HttpSession session = request.getSession()
            IdentityAPI identityAPI = TenantAPIAccessor.getIdentityAPI(pageContext.getApiSession())
            User user = identityAPI.getUserByUserName((String)session.getAttribute("username"))
            PrintWriter out = response.getWriter()
            out.write("<div class=\"user\">")
            out.write("<span>User details returned from Engine API Call using the existing API Session:</span>")
            out.write(user.toString())
            out.write("</div>")
            out.flush()
            out.close()
        } catch (Exception e) {
            e.printStackTrace()
        }
    }

}
