package cc.openstrata.platform.web;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * RBAC gate for the CONSUMER role (EU-05). Consumers may only browse the agent
 * catalog and open chat sessions; they cannot build, publish, or administer.
 * The role is read directly from the {@code X-Role} header (set by the gateway
 * Auth SPI) so the check is independent of interceptor ordering.
 */
public class ConsumerAccessFilter extends OncePerRequestFilter {
    public static final String HEADER_ROLE = "X-Role";

    private static final List<String> CONSUMER_PREFIXES = List.of(
        "/api/v1/agents", "/api/v1/chat", "/api/v1/files");

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
            throws ServletException, IOException {
        String role = req.getHeader(HEADER_ROLE);
        if ("consumer".equals(role)) {
            String path = req.getServletPath();
            boolean allowed = "GET".equals(req.getMethod())
                && CONSUMER_PREFIXES.stream().anyMatch(path::startsWith);
            if (!allowed) {
                res.sendError(HttpServletResponse.SC_FORBIDDEN,
                    "consumer role may only browse agents and open chat");
                return;
            }
        }
        chain.doFilter(req, res);
    }
}
