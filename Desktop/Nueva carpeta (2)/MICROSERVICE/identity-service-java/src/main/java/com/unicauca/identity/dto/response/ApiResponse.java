package com.unicauca.identity.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Wrapper genérico para todas las respuestas API
 * @param <T> Tipo de datos que contiene la respuesta
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApiResponse<T> {
    private boolean success;
    private String message;
    private T data;
    private List<String> errors;

    // Getters y setters manuales
    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public List<String> getErrors() {
        return errors;
    }

    public void setErrors(List<String> errors) {
        this.errors = errors;
    }

    // Implementación manual del patrón builder genérico
    public static <T> ApiResponseBuilder<T> builder() {
        return new ApiResponseBuilder<>();
    }

    public static class ApiResponseBuilder<T> {
        private final ApiResponse<T> response = new ApiResponse<>();

        public ApiResponseBuilder<T> success(boolean success) {
            response.setSuccess(success);
            return this;
        }

        public ApiResponseBuilder<T> message(String message) {
            response.setMessage(message);
            return this;
        }

        public ApiResponseBuilder<T> data(T data) {
            response.setData(data);
            return this;
        }

        public ApiResponseBuilder<T> errors(List<String> errors) {
            response.setErrors(errors);
            return this;
        }

        public ApiResponse<T> build() {
            return response;
        }
    }

    /**
     * Crea una respuesta de éxito con datos
     *
     * @param data Los datos a devolver
     * @param message Mensaje descriptivo opcional
     * @return ApiResponse con indicación de éxito
     * @param <T> Tipo de datos
     */
    public static <T> ApiResponse<T> success(T data, String message) {
        return ApiResponse.<T>builder()
                .success(true)
                .message(message)
                .data(data)
                .build();
    }

    /**
     * Crea una respuesta de éxito con datos pero sin mensaje
     *
     * @param data Los datos a devolver
     * @return ApiResponse con indicación de éxito
     * @param <T> Tipo de datos
     */
    public static <T> ApiResponse<T> success(T data) {
        return success(data, null);
    }

    /**
     * Crea una respuesta de error con mensaje pero sin datos
     *
     * @param message Mensaje de error
     * @return ApiResponse con indicación de error
     * @param <T> Tipo de datos (null en este caso)
     */
    public static <T> ApiResponse<T> error(String message) {
        return ApiResponse.<T>builder()
                .success(false)
                .message(message)
                .build();
    }

    /**
     * Crea una respuesta de error con mensaje y lista de errores
     *
     * @param message Mensaje general de error
     * @param errors Lista de errores específicos
     * @return ApiResponse con indicación de error y lista de errores
     * @param <T> Tipo de datos (null en este caso)
     */
    public static <T> ApiResponse<T> error(String message, List<String> errors) {
        return ApiResponse.<T>builder()
                .success(false)
                .message(message)
                .errors(errors)
                .build();
    }
}
