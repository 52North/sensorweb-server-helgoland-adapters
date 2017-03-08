package org.n52.proxy.cache;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Set;

public class CachingFilter implements Filter {
    private static final Logger LOG = LoggerFactory.getLogger(CachingFilter.class);

    private SimpleCache cache = new SimpleCache();

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        LOG.info("Caching filter initialized");
    }

    /**
     * 1. Check if httprequest
     * 2. Check that it's a data request
     * 3. Check timespan param
     * 4. Go to the cache for that values
     * 5. Return values or continue request processing
     * 6. Write returned values to the cache
     * @param servletRequest
     * @param servletResponse
     * @param filterChain
     * @throws IOException
     * @throws ServletException
     */
    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        LOG.info("Doing caching filtering");

        if (!(servletRequest instanceof HttpServletRequest)) {
            filterChain.doFilter(servletRequest, servletResponse);
            return;
        }

        HttpServletRequest request = (HttpServletRequest) servletRequest;
        if (!request.getPathInfo().endsWith("/data")) {
            filterChain.doFilter(servletRequest, servletResponse);
            return;
        }

        Set<String> requestParametersNames = request.getParameterMap().keySet();
        if (!requestParametersNames.contains("timespan")) {
            filterChain.doFilter(servletRequest, servletResponse);
            return;
        }

        filterChain.doFilter(servletRequest, servletResponse);
        LOG.info("End of caching filtering");
    }

    @Override
    public void destroy() {
        LOG.info("Destroying caching filter");
    }
}
