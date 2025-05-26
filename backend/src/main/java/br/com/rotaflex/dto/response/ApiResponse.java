package br.com.rotaflex.dto.response;
// Defina a classe ApiResponse
public class ApiResponse {
    private int code;
    private String message;

    // Construtor
    public ApiResponse(int code, String message) {
        this.code = code;
        this.message = message;
    }

    // Getters e setters
    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}

