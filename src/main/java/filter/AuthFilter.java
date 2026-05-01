package filter;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
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

            // التحقق مما إذا كان المستخدم مسجلاً الدخول
            boolean loggedIn = (session != null && session.getAttribute("user") != null);
            
            // السماح بالوصول لصفحات تسجيل الدخول والتسجيل
            boolean loginRequest = reqURI.contains("/login.xhtml");
            boolean registerRequest = reqURI.contains("/register.xhtml");
            
            // السماح بالوصول لموارد النظام (مثل ملفات CSS)
            boolean resourceRequest = reqURI.startsWith(req.getContextPath() + "/resources/") || reqURI.startsWith(req.getContextPath() + "/jakarta.faces.resource/");

            if (loggedIn || loginRequest || registerRequest || resourceRequest) {
                // إذا كان المستخدم مسجلاً للدخول، أو يطلب صفحة مسموحة، دعه يمر
                chain.doFilter(request, response);
            } else {
                // إذا لم يكن مسجلاً ويطلب صفحة محمية (مثل لوحة التحكم)، أعد توجيهه لتسجيل الدخول
                res.sendRedirect(req.getContextPath() + "/login.xhtml");
            }
        } catch (Exception e) {
            System.out.println("Exception in AuthFilter: " + e.getMessage());
        }
    }

    @Override
    public void destroy() {
    }
}
