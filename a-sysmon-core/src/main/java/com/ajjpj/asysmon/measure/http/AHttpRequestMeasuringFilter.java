package com.ajjpj.asysmon.measure.http;

import com.ajjpj.asysmon.ASysMon;
import com.ajjpj.asysmon.measure.ASimpleMeasurement;
import com.sun.deploy.net.HttpRequest;

import javax.servlet.*;
import java.io.IOException;
import java.util.Map;

/**
 * @author arno
 */
public class AHttpRequestMeasuringFilter implements Filter {
    public static final String PARAM_ANALYZER_CLASS_FQN = "asysmon.http.analyzer";

    private AHttpRequestAnalyzer analyzer;

    @Override public void init(FilterConfig filterConfig) throws ServletException {
        try {
            analyzer = createAnalyzer(filterConfig);
        } catch (Exception e) {
            throw new ServletException(e);
        }
    }

    /**
     * override to customize
     */
    protected AHttpRequestAnalyzer createAnalyzer(FilterConfig filterConfig) throws Exception {
        final String analyzerFqn = filterConfig.getInitParameter(PARAM_ANALYZER_CLASS_FQN);

        if(analyzerFqn != null) {
            return (AHttpRequestAnalyzer) Thread.currentThread().getContextClassLoader().loadClass(analyzerFqn).newInstance();
        }

        return new ASimpleHttpRequestAnalyzer();
    }

    /**
     * override to customize
     */
    protected ASysMon getSysMon() {
        return ASysMon.get();
    }

    @Override public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        final AHttpRequestDetails details = analyzer.analyze((HttpRequest) servletRequest);

        final ASimpleMeasurement measurement = getSysMon().start(details.getIdentifier());
        try {
            for(Map.Entry<String, String> entry: details.getParameters().entrySet()) {
                measurement.addParameter(entry.getKey(), entry.getValue());
            }

            filterChain.doFilter(servletRequest, servletResponse);
        }
        finally {
            measurement.finish();
        }
    }

    @Override public void destroy() {
    }
}
