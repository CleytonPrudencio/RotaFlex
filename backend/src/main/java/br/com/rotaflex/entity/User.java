package br.com.rotaflex.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "TB_USUARIO", schema = "rotaflex")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "usuario_seq")
    @SequenceGenerator(
            name = "usuario_seq",
            sequenceName = "rotaflex.TB_USUARIO_id_seq",
            allocationSize = 1
    )
    @Column(name = "ID")
    private Long id;

    @NotBlank
    @Column(name = "NOME", nullable = false)
    private String username;

    @NotBlank
    @Column(name = "SENHA", nullable = false)
    private String password;

    @NotBlank
    @Column(name = "SOBRENOME", nullable = false)
    private String sobrenome;

    @Email
    @Column(name = "EMAIL")
    private String email;

    @Column(name = "CPF", length = 11)
    private String cpf;

    @Column(name = "TELEFONE")
    private String telefone;

    @Column(name = "CEP")
    private String cep;

    @Column(name = "LOGRADOURO")
    private String logradouro;

    @Column(name = "CIDADE")
    private String cidade;

    @Column(name = "ESTADO")
    private String estado;

    @Column(name = "BAIRRO")
    private String bairro;

    @Column(name = "NUMERO")
    private String numero;

    @Column(name = "COMPLEMENTO")
    private String complemento;

    @Column(name = "GENERO")
    private String genero;

    @Column(name = "ALERTA")
    private Boolean alerta;

    @Column(name = "ATIVO", nullable = false)
    private Boolean ativo;

    @Column(name = "DATA_CRIACAO")
    private LocalDateTime dataCriacao;

    @Column(name = "DATA_ATUALIZACAO")
    private LocalDateTime dataAtualizacao;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "TIPO", referencedColumnName = "ID")
    private Role role;

    // Para preenchimento automático de data_criacao e data_atualizacao
    @PrePersist
    protected void onCreate() {
        this.dataCriacao = LocalDateTime.now();
        this.dataAtualizacao = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.dataAtualizacao = LocalDateTime.now();
    }
}
