package br.com.rotaflex.dto.request;

import lombok.Data;

@Data
public class UserUpdateRequest {


    private String username;
    private String email;
    private String cpf;
    private int tipo; // Tipo do usuário (1, 2, 3, 4, 5)
    private String departamento;
    private String cargo;
    private String delegacia;
    private String distintivo;
    private String ra;
}
