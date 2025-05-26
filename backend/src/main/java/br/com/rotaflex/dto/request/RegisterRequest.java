package br.com.rotaflex.dto.request;

import lombok.Data;

import javax.validation.constraints.*;

@Data
public class RegisterRequest {

    @NotBlank(message = "Nome é obrigatório")
    private String username;

    @NotBlank(message = "Sobrenome é obrigatório")
    private String sobrenome;

    @NotBlank(message = "Senha é obrigatória")
    private String password;

    @NotBlank(message = "Email é obrigatório")
    @Email(message = "Email inválido")
    private String email;

    @NotBlank(message = "CPF é obrigatório")
    @Pattern(regexp = "^[0-9]{11}$", message = "CPF deve conter exatamente 11 dígitos")
    private String cpf;

    @NotBlank(message = "Telefone é obrigatório")
    private String telefone;

    @NotBlank(message = "CEP é obrigatório")
    private String cep;

    @NotBlank(message = "Logradouro é obrigatório")
    private String logradouro;

    @NotBlank(message = "Cidade é obrigatória")
    private String cidade;

    @NotBlank(message = "Estado é obrigatório")
    private String estado;

    @NotBlank(message = "Bairro é obrigatório")
    private String bairro;

    @NotBlank(message = "Número é obrigatório")
    private String numero;

    private String complemento;

    @NotBlank(message = "Gênero é obrigatório")
    private String genero;

    private Boolean alerta;

    @NotNull(message = "Tipo de usuário é obrigatório")
    private Long tipo; // ID do Role (ADMIN = 1, ADMINISTRATIVO = 2, por exemplo)
}
