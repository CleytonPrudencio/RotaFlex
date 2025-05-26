package br.com.rotaflex.exception;

public class ApiException extends RuntimeException {
    private final int status;
    private final String error;
    private final String message;

    public ApiException(int status, String error, String message) {
        super(message);
        this.status = status;
        this.error = error;
        this.message = message;
    }

    public int getStatus() {
        return status;
    }

    public String getError() {
        return error;
    }

    @Override
    public String getMessage() {
        return message;
    }

    // 400 Bad Request
    public static class BadRequestException extends ApiException {
        public BadRequestException(String message) {
            super(400, "Bad Request", message);
        }
        public BadRequestException() {
            this("Requisição inválida.");
        }
    }

    // 401 Unauthorized
    public static class UnauthorizedException extends ApiException {
        public UnauthorizedException(String message) {
            super(401, "Unauthorized", message);
        }
        public UnauthorizedException() {
            this("Acesso não autorizado.");
        }
    }

    // 403 Forbidden
    public static class ForbiddenException extends ApiException {
        public ForbiddenException(String message) {
            super(403, "Forbidden", message);
        }
        public ForbiddenException() {
            this("Acesso proibido.");
        }
    }

    // 404 Not Found
    public static class NotFoundException extends ApiException {
        public NotFoundException(String message) {
            super(404, "Not Found", message);
        }
        public NotFoundException() {
            this("Recurso não encontrado.");
        }
    }

    // 405 Method Not Allowed
    public static class MethodNotAllowedException extends ApiException {
        public MethodNotAllowedException(String message) {
            super(405, "Method Not Allowed", message);
        }
        public MethodNotAllowedException() {
            this("Método HTTP não permitido.");
        }
    }

    // 409 Conflict
    public static class ConflictException extends ApiException {
        public ConflictException(String message) {
            super(409, "Conflict", message);
        }
        public ConflictException() {
            this("Conflito de dados.");
        }
    }

    // 415 Unsupported Media Type
    public static class UnsupportedMediaTypeException extends ApiException {
        public UnsupportedMediaTypeException(String message) {
            super(415, "Unsupported Media Type", message);
        }
        public UnsupportedMediaTypeException() {
            this("Tipo de mídia não suportado.");
        }
    }

    // 422 Unprocessable Entity
    public static class UnprocessableEntityException extends ApiException {
        public UnprocessableEntityException(String message) {
            super(422, "Unprocessable Entity", message);
        }
        public UnprocessableEntityException() {
            this("Entidade não processável.");
        }
    }

    // 500 Internal Server Error
    public static class InternalServerErrorException extends ApiException {
        public InternalServerErrorException(String message) {
            super(500, "Internal Server Error", message);
        }
        public InternalServerErrorException() {
            this("Erro interno do servidor.");
        }
    }

    // 503 Service Unavailable
    public static class ServiceUnavailableException extends ApiException {
        public ServiceUnavailableException(String message) {
            super(503, "Service Unavailable", message);
        }
        public ServiceUnavailableException() {
            this("Serviço indisponível no momento.");
        }
    }

    // 504 Gateway Timeout
    public static class GatewayTimeoutException extends ApiException {
        public GatewayTimeoutException(String message) {
            super(504, "Gateway Timeout", message);
        }
        public GatewayTimeoutException() {
            this("Tempo de resposta do gateway esgotado.");
        }
    }
}
