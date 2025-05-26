package br.com.rotaflex.dto.request;

import lombok.Data;

@Data
public class UserUpdateRequest {

    private String username;
    private String sobrenome;
    private String email;
    private String cpf;
    private String telefone;
    private String cep;
    private String logradouro;
    private String cidade;
    private String estado;
    private String bairro;
    private String numero;
    private String complemento;
    private String genero;
    private Boolean alerta;

    private int tipo; // 1 = ADMIN, 2 = ADMINISTRATIVO

    // Campos adicionais para ADMINISTRATIVO
    private String delegacia;    // = delegate
    private String distintivo;   // = badge
    private String ra;
    private String departamento;
    private String cargo;
}
