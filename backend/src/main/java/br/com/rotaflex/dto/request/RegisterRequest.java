package br.com.rotaflex.dto.request;

import lombok.Data;

import javax.validation.constraints.*;

@Data
public class RegisterRequest {

    @NotBlank(message = "Username is required")
    private String username;

    @NotBlank(message = "Password is required")
    private String password;

    @Email(message = "Email should be valid")
    private String email;

    @NotBlank(message = "CPF is required")
    @Pattern(regexp = "^[0-9]{11}$", message = "CPF must contain only 11 digits")
    private String cpf;

    @NotNull(message = "Tipo is required")
    private Integer tipo;

    // Campos adicionais para Policial, Gestor, etc.
    private String delegacia;
    private String distintivo;
    private String ra;
    private String departamento;
    private String cargo;

    @AssertTrue(message = "Campos obrigatórios não preenchidos corretamente")
    public boolean isValid() {
        // Verifica se os campos obrigatórios estão preenchidos com base no tipo de usuário
        if (tipo == null) {
            return false;
        }

        // Para tipo = 2 (Policial), os campos delegacia, distintivo e ra são obrigatórios
        if (tipo == 2 && (delegacia == null || delegacia.isEmpty() || distintivo == null || distintivo.isEmpty() || ra == null || ra.isEmpty())) {
            return false;
        }

        // Para tipo = 5 (Gestor), os campos departamento e cargo são obrigatórios
        if (tipo == 5 && (departamento == null || departamento.isEmpty() || cargo == null || cargo.isEmpty())) {
            return false;
        }

        return true;
    }
}
