package br.com.rotaflex.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Table(name = "TB_USUARIO_ROLE")
@Getter
@Setter
public class Role {

    @Id
    private Long id;

    @Column(name = "ROLE", nullable = false, unique = true, length = 100)
    private String name;
}

