package cc.openstrata.platform.web;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ConsumerAccessFilterTest {
    private final ConsumerAccessFilter filter = new ConsumerAccessFilter();

    @Test
    void consumerBlockedOnPostToProtected() throws Exception {
        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpServletResponse res = mock(HttpServletResponse.class);
        FilterChain chain = mock(FilterChain.class);
        when(req.getHeader("X-Role")).thenReturn("consumer");
        when(req.getServletPath()).thenReturn("/api/v1/agents");
        when(req.getMethod()).thenReturn("POST");

        filter.doFilter(req, res, chain);

        verify(res).sendError(eq(403), anyString());
        verify(chain, never()).doFilter(any(), any());
    }

    @Test
    void consumerAllowedOnGetAgents() throws Exception {
        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpServletResponse res = mock(HttpServletResponse.class);
        FilterChain chain = mock(FilterChain.class);
        when(req.getHeader("X-Role")).thenReturn("consumer");
        when(req.getServletPath()).thenReturn("/api/v1/agents");
        when(req.getMethod()).thenReturn("GET");

        filter.doFilter(req, res, chain);

        verify(chain).doFilter(req, res);
    }

    @Test
    void nonConsumerPassesThrough() throws Exception {
        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpServletResponse res = mock(HttpServletResponse.class);
        FilterChain chain = mock(FilterChain.class);
        when(req.getHeader("X-Role")).thenReturn("developer");
        when(req.getServletPath()).thenReturn("/api/v1/agents");
        when(req.getMethod()).thenReturn("POST");

        filter.doFilter(req, res, chain);

        verify(chain).doFilter(req, res);
    }
}
