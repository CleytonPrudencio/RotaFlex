package br.com.rotaflex.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

@Getter
@Setter
@Entity
@Table(name = "TB_USUARIO")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "usuario_seq")
    @SequenceGenerator(name = "usuario_seq", sequenceName = "ISEQ$$_76217", allocationSize = 1)
    @Column(name = "ID")
    private Long id;

    @NotBlank
    @Column(name = "NOME")
    private String username;

    @NotBlank
    @Column(name = "SENHA")
    private String password;

    @Email
    @Column(name = "EMAIL")
    private String email;

    // Adicionando os campos CPF e ALERTA
    @Column(name = "CPF", length = 11)
    private String cpf;

    @Column(name = "ALERTA")
    private Boolean alerta; // ALERTA pode ser true ou false

    // Relacionamento com a role (papel do usuário)
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "TIPO", referencedColumnName = "ID")
    private Role role;

    // Campos adicionais para perfis específicos

    @Column(name = "DELEGACIA")
    private String delegate; // Para Policial, Agente de Segurança, Investigador

    @Column(name = "DISTINTIVO")
    private String badge; // Para Policial, Agente de Segurança, Investigador

    @Column(name = "RA")
    private String ra; // Para Policial, Agente de Segurança, Investigador

    @Column(name = "DEPARTAMENTO")
    private String departamento; // Para Gestor de Segurança Pública

    @Column(name = "CARGO")
    private String cargo; // Para Gestor de Segurança Pública

    @Column(name = "ATIVO")
    private Boolean ativo;
}
