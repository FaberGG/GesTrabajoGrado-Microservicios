package co.unicauca.microservice.noti_service.http;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

/**
 * Filtro que inyecta X-Correlation-Id por request/respuesta.
 * Usamos un nombre de bean distinto para no chocar con el de Spring Boot.
 */
@Component("correlationIdFilter")
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RequestContextFilter extends OncePerRequestFilter {
  @Override
  protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
      throws ServletException, IOException {
    String correlationId = req.getHeader("X-Correlation-Id");
    if (!StringUtils.hasText(correlationId)) {
      correlationId = UUID.randomUUID().toString();
    }
    MDC.put("correlationId", correlationId);
    res.setHeader("X-Correlation-Id", correlationId);
    try {
      chain.doFilter(req, res);
    } finally {
      MDC.clear();
    }
  }
}