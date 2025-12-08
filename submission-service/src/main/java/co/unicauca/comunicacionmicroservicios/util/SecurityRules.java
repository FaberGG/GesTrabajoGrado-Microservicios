package co.unicauca.comunicacionmicroservicios.util;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public final class SecurityRules {
    private SecurityRules() {}

    public static void requireDocente(String role) {
        if (role == null || !role.equalsIgnoreCase("DOCENTE")) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Solo DOCENTE puede realizar esta acción");
        }
    }

    // Mientras no tengas OAuth m2m, validamos con header interno
    public static void requireInternalReviewService(String callerHeader) {
        if (callerHeader == null || callerHeader.isBlank() || !callerHeader.equalsIgnoreCase("review")) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Acceso restringido al servicio de revisión");
        }
    }
}
