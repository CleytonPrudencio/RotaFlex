package br.com.rotaflex.entity;

import lombok.Data;

import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "TB_PASSWORD_RESET_TOKEN")
public class PasswordResetToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String token;

    @ManyToOne
    private User user;

    private LocalDateTime expiryDate;

    private boolean used = false;

    public PasswordResetToken() {
    }

    public PasswordResetToken(String token, User user, LocalDateTime expiryDate, boolean used) {
        this.token = token;
        this.user = user;
        this.expiryDate = expiryDate;
        this.used = used;
    }
}
