package br.com.rotaflex.dto.request;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class ResetarSenhaRequest {
    private String token;
    private String novaSenha;

}

