package me.andreaseriksson.ufodashboard.web;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;

/**
 * Global API error handling for the dashboard backend.
 *
 * Converts backend and upstream API failures into consistent JSON responses so the frontend
 * can show meaningful error messages instead of only logging stack traces.
 */
@RestControllerAdvice
public class ApiExceptionHandler {

    /**
     * Handles upstream 404 responses from the WT1 API.
     *
     * @param ex the not-found exception from RestTemplate
     * @param request the current HTTP request
     * @return a JSON error response with HTTP 404
     */
    @ExceptionHandler(HttpClientErrorException.NotFound.class)
    public ResponseEntity<Map<String, Object>> handleNotFound(HttpClientErrorException.NotFound ex, HttpServletRequest request) {
        return buildErrorResponse(HttpStatus.NOT_FOUND, "The requested resource was not found.", request, ex.getMessage());
    }

    /**
     * Handles other client-side errors returned by the upstream WT1 API.
     *
     * @param ex the client error from RestTemplate
     * @param request the current HTTP request
     * @return a JSON error response using the upstream status code
     */
    @ExceptionHandler(HttpClientErrorException.class)
    public ResponseEntity<Map<String, Object>> handleClientError(HttpClientErrorException ex, HttpServletRequest request) {
        return buildErrorResponse(ex.getStatusCode(), "The upstream API rejected the request.", request, ex.getMessage());
    }

    /**
     * Handles upstream server-side errors.
     *
     * @param ex the server error from RestTemplate
     * @param request the current HTTP request
     * @return a JSON error response with HTTP 502
     */
    @ExceptionHandler(HttpServerErrorException.class)
    public ResponseEntity<Map<String, Object>> handleServerError(HttpServerErrorException ex, HttpServletRequest request) {
        return buildErrorResponse(HttpStatus.BAD_GATEWAY, "The upstream API is temporarily unavailable.", request, ex.getMessage());
    }

    /**
     * Handles network and connection problems when calling the upstream API.
     *
     * @param ex the connection error from RestTemplate
     * @param request the current HTTP request
     * @return a JSON error response with HTTP 503
     */
    @ExceptionHandler(ResourceAccessException.class)
    public ResponseEntity<Map<String, Object>> handleResourceAccessError(ResourceAccessException ex, HttpServletRequest request) {
        return buildErrorResponse(HttpStatus.SERVICE_UNAVAILABLE, "The upstream API could not be reached.", request, ex.getMessage());
    }

    /**
     * Handles access-denied errors for secured endpoints.
     *
     * @param ex the access denied exception
     * @param request the current HTTP request
     * @return a JSON error response with HTTP 403
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, Object>> handleAccessDenied(AccessDeniedException ex, HttpServletRequest request) {
        return buildErrorResponse(HttpStatus.FORBIDDEN, "You do not have permission to access this resource.", request, ex.getMessage());
    }

    /**
     * Handles unexpected server errors.
     *
     * @param ex the unexpected exception
     * @param request the current HTTP request
     * @return a JSON error response with HTTP 500
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGenericError(Exception ex, HttpServletRequest request) {
        return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred.", request, ex.getMessage());
    }

    private ResponseEntity<Map<String, Object>> buildErrorResponse(HttpStatusCode status, String message, HttpServletRequest request, String details) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", Instant.now().toString());
        body.put("status", status.value());
        body.put("error", status.toString());
        body.put("message", message);
        if (details != null && !details.isBlank()) {
            body.put("details", details);
        }
        body.put("path", request.getRequestURI());
        return ResponseEntity.status(status).body(body);
    }
}


