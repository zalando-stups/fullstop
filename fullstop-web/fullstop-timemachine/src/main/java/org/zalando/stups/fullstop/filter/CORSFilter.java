package org.zalando.stups.fullstop.filter;

import org.springframework.stereotype.Component;

import javax.servlet.*;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Created by gkneitschel.
 */

/**
 * A simple filter to allow  Cross-Origin Resource Sharing (CORS)
 */
@Component
public class CORSFilter implements Filter {
    public void doFilter(final ServletRequest req, final ServletResponse res, final FilterChain chain)
            throws IOException, ServletException {
        final HttpServletResponse response = (HttpServletResponse) res;
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Methods", "POST, GET, OPTIONS, DELETE, PUT");
        response.setHeader("Access-Control-Max-Age", "3600");
        response.setHeader("Access-Control-Allow-Headers", "Content-Type, x-requested-with, Authorization");
        chain.doFilter(req, res);
    }

    public void init(final FilterConfig filterConfig) {
    }

    public void destroy() {
    }
}
