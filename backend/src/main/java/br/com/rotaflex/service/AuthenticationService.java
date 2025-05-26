package br.com.rotaflex.service;

import br.com.rotaflex.dto.request.RegisterRequest;
import br.com.rotaflex.dto.request.UserUpdateRequest;
import br.com.rotaflex.entity.PasswordResetToken;
import br.com.rotaflex.entity.Role;
import br.com.rotaflex.entity.RoleType;
import br.com.rotaflex.entity.User;
import br.com.rotaflex.exception.ApiException;
import br.com.rotaflex.repository.RoleRepository;
import br.com.rotaflex.repository.TokenRepository;
import br.com.rotaflex.repository.UserRepository;
import br.com.rotaflex.security.JwtTokenUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class AuthenticationService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final JwtTokenUtil jwtTokenUtil;
    private final RoleRepository roleRepository;
    private final TokenRepository tokenRepository;

    @Autowired
    public AuthenticationService(UserRepository userRepository,
                                 BCryptPasswordEncoder passwordEncoder,
                                 JwtTokenUtil jwtTokenUtil,
                                 RoleRepository roleRepository,
                                 TokenRepository tokenRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenUtil = jwtTokenUtil;
        this.roleRepository = roleRepository;
        this.tokenRepository = tokenRepository;
    }

    public String authenticate(String username, String password) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ApiException.NotFoundException("Usuário não encontrado: " + username));

        if (!Boolean.TRUE.equals(user.getAtivo())) {
            throw new ApiException.ForbiddenException("Usuário está inativo.");
        }

        if (passwordEncoder.matches(password, user.getPassword())) {
            return jwtTokenUtil.generateToken(user);
        } else {
            throw new ApiException.UnauthorizedException("Credenciais inválidas.");
        }
    }

    public String generateToken(String cpf, String password) {
        User user = userRepository.findByCpf(cpf)
                .orElseThrow(() -> new ApiException.NotFoundException("Usuário não encontrado pelo CPF: " + cpf));

        if (!Boolean.TRUE.equals(user.getAtivo())) {
            throw new ApiException.ForbiddenException("Usuário está inativo.");
        }

        if (passwordEncoder.matches(password, user.getPassword())) {
            return jwtTokenUtil.generateToken(user);
        } else {
            throw new ApiException.UnauthorizedException("CPF ou senha inválidos.");
        }
    }

    public void register(RegisterRequest registerRequest) {
        userRepository.findByCpf(registerRequest.getCpf()).ifPresent(user -> {
            if (Boolean.TRUE.equals(user.getAtivo())) {
                throw new ApiException.ConflictException("Usuário já registrado e ativo.");
            }
            user.setAtivo(true);
            updateUserFromRegisterRequest(user, registerRequest);
            userRepository.save(user);
            throw new ApiException.BadRequestException("Usuário reativado com sucesso."); // Ou apenas logue, não jogue erro se for sucesso
        });

        User user = new User();
        user.setAtivo(true);
        updateUserFromRegisterRequest(user, registerRequest);

        RoleType roleType = RoleType.fromId(registerRequest.getTipo());
        Role role = roleRepository.findById((long) roleType.getId())
                .orElseThrow(() -> new ApiException.NotFoundException("Perfil não encontrado com ID: " + roleType.getId()));

        user.setRole(role);
        userRepository.save(user);
    }

    private void updateUserFromRegisterRequest(User user, RegisterRequest req) {
        user.setCpf(req.getCpf());
        user.setUsername(req.getUsername());
        user.setSobrenome(req.getSobrenome());
        user.setPassword(passwordEncoder.encode(req.getPassword()));
        user.setEmail(req.getEmail());
        user.setTelefone(req.getTelefone());
        user.setCep(req.getCep());
        user.setLogradouro(req.getLogradouro());
        user.setCidade(req.getCidade());
        user.setEstado(req.getEstado());
        user.setBairro(req.getBairro());
        user.setNumero(req.getNumero());
        user.setComplemento(req.getComplemento());
        user.setGenero(req.getGenero());
        user.setAlerta(req.getAlerta());
    }

    public User getUserDetails(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ApiException.NotFoundException("Usuário não encontrado: " + username));
    }

    public Map<String, Object> forgotPassword(String emailCpf) {
        User user;
        if (emailCpf.contains("@")) {
            user = userRepository.findByEmail(emailCpf)
                    .orElseThrow(() -> new ApiException.NotFoundException("Usuário com esse e-mail não encontrado."));
        } else {
            user = userRepository.findByCpf(emailCpf)
                    .orElseThrow(() -> new ApiException.NotFoundException("Usuário com esse CPF não encontrado."));
        }

        String token = UUID.randomUUID().toString();

        PasswordResetToken resetToken = new PasswordResetToken(
                token,
                user,
                LocalDateTime.now().plusHours(1),
                false
        );

        tokenRepository.save(resetToken);

        Map<String, Object> usuario = new HashMap<>();
        usuario.put("nome", user.getUsername());
        usuario.put("email", user.getEmail());
        usuario.put("cpf", user.getCpf());
        usuario.put("permissao", user.getRole().getName());
        usuario.put("token", token);

        return usuario;
    }

    public boolean resetarSenha(String token, String novaSenha) {
        PasswordResetToken resetToken = tokenRepository.findByToken(token)
                .orElseThrow(() -> new ApiException.NotFoundException("Token de redefinição de senha inválido."));

        if (resetToken.isUsed()) {
            throw new ApiException.BadRequestException("Token já foi utilizado.");
        }

        if (resetToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            throw new ApiException.BadRequestException("Token expirado.");
        }

        User user = resetToken.getUser();
        user.setPassword(passwordEncoder.encode(novaSenha));
        userRepository.save(user);

        resetToken.setUsed(true);
        tokenRepository.save(resetToken);

        return true;
    }

    public boolean updateUserData(String id, UserUpdateRequest userUpdateRequest) {
        User user = userRepository.findById(Long.valueOf(id))
                .orElseThrow(() -> new ApiException.NotFoundException("Usuário não encontrado."));

        RoleType roleType = switch (userUpdateRequest.getTipo()) {
            case 1 -> RoleType.ADMIN;
            case 2 -> RoleType.ADMINISTRATIVO;
            default -> throw new ApiException.BadRequestException("Tipo de usuário inválido.");
        };

        Role role = roleRepository.findById((long) roleType.getId())
                .orElseThrow(() -> new ApiException.NotFoundException("Perfil não encontrado: " + roleType));

        // Atualiza dados básicos
        user.setUsername(userUpdateRequest.getUsername());
        user.setSobrenome(userUpdateRequest.getSobrenome());
        user.setEmail(userUpdateRequest.getEmail());
        user.setCpf(userUpdateRequest.getCpf());
        user.setTelefone(userUpdateRequest.getTelefone());
        user.setCep(userUpdateRequest.getCep());
        user.setLogradouro(userUpdateRequest.getLogradouro());
        user.setCidade(userUpdateRequest.getCidade());
        user.setEstado(userUpdateRequest.getEstado());
        user.setBairro(userUpdateRequest.getBairro());
        user.setNumero(userUpdateRequest.getNumero());
        user.setComplemento(userUpdateRequest.getComplemento());
        user.setGenero(userUpdateRequest.getGenero());
        user.setAlerta(userUpdateRequest.getAlerta());
        user.setRole(role);

        userRepository.save(user);
        return true;
    }

}
