package filter;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

@WebFilter(filterName = "AuthFilter", urlPatterns = {"*.xhtml"})
public class AuthFilter implements Filter {

    public AuthFilter() {
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        try {
            HttpServletRequest req = (HttpServletRequest) request;
            HttpServletResponse res = (HttpServletResponse) response;
            HttpSession session = req.getSession(false);

            String reqURI = req.getRequestURI();

            // Check if user is logged in
            boolean loggedIn = (session != null && session.getAttribute("user") != null);

            // Allow access to login and register pages
            boolean loginRequest = reqURI.contains("/login.xhtml");
            boolean registerRequest = reqURI.contains("/register.xhtml");

            // Allow access to system resources (like CSS files)
            boolean resourceRequest = reqURI.startsWith(req.getContextPath() + "/resources/") || reqURI.startsWith(req.getContextPath() + "/jakarta.faces.resource/");

            if (loggedIn || loginRequest || registerRequest || resourceRequest) {
                // If user is logged in, or requesting allowed pages, let them pass
                chain.doFilter(request, response);
            } else {
                // If not logged in and requesting a protected page (like dashboard), redirect to login
                HttpServletResponse httpResponse = (HttpServletResponse) response;
                httpResponse.sendRedirect(req.getContextPath() + "/login.xhtml");
            }
        } catch (Exception e) {
            System.out.println("Exception in AuthFilter: " + e.getMessage());
        }
    }

    @Override
    public void destroy() {
    }
}