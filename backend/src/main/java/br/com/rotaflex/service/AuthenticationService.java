package br.com.rotaflex.service;

import br.com.rotaflex.dto.request.RegisterRequest;
import br.com.rotaflex.dto.request.UserUpdateRequest;
import br.com.rotaflex.entity.PasswordResetToken;
import br.com.rotaflex.entity.Role;
import br.com.rotaflex.entity.RoleType;
import br.com.rotaflex.entity.User;
import br.com.rotaflex.repository.RoleRepository;
import br.com.rotaflex.repository.TokenRepository;
import br.com.rotaflex.repository.UserRepository;
import br.com.rotaflex.security.JwtTokenUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class AuthenticationService {

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.expiration}")
    private long jwtExpiration;

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final JwtTokenUtil jwtTokenUtil;
    private final RoleRepository roleRepository;
    private final TokenRepository tokenRepository;

    @Autowired
    public AuthenticationService(UserRepository userRepository, BCryptPasswordEncoder passwordEncoder, JwtTokenUtil jwtTokenUtil, RoleRepository roleRepository, TokenRepository tokenRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenUtil = jwtTokenUtil;
        this.roleRepository = roleRepository;
        this.tokenRepository = tokenRepository;
    }

    public String authenticate(String username, String password) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User  not found"));

        if (passwordEncoder.matches(password, user.getPassword())) {
            return jwtTokenUtil.generateToken(user);
        } else {
            throw new RuntimeException("Invalid credentials");
        }
    }

    public String generateToken(String cpf, String password) {
        Optional<User> userOpt = userRepository.findByCpf(cpf);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            if (passwordEncoder.matches(password, user.getPassword())) {
                return jwtTokenUtil.generateToken(user);
            }
        }
        throw new RuntimeException("Invalid username or password");
    }

    public void register(RegisterRequest registerRequest) {
        String encodedPassword = passwordEncoder.encode(registerRequest.getPassword());
        Optional<User> existingUserOpt = userRepository.findByCpf(registerRequest.getCpf());

        User user;

        if (existingUserOpt.isPresent()) {
            user = existingUserOpt.get();

            if (Boolean.TRUE.equals(user.getAtivo())) {
                throw new RuntimeException("Usuário já registrado e ativo.");
            }

            // Reativar usuário
            user.setAtivo(true);
        } else {
            user = new User();
            user.setCpf(registerRequest.getCpf());
            user.setAtivo(true); // novo usuário já vem ativo
        }

        // Dados comuns (usado em criação ou reativação)
        user.setUsername(registerRequest.getUsername());
        user.setPassword(encodedPassword);
        user.setEmail(registerRequest.getEmail());

        // Papel (role) do usuário
        RoleType roleType = switch (registerRequest.getTipo()) {
            case 1 -> RoleType.PUBLICO;
            case 2 -> RoleType.POLICIAL;
            case 3 -> RoleType.AGENTE_DE_SEGURANCA;
            case 4 -> RoleType.INVESTIGADOR;
            case 5 -> RoleType.GESTOR_DE_SEGURANCA_PUBLICA;
            default -> throw new IllegalArgumentException("Tipo de usuário inválido.");
        };

        Role role = roleRepository.findByName(roleType)
                .orElseThrow(() -> new RuntimeException("Papel não encontrado: " + roleType));
        user.setRole(role);

        // Campos adicionais obrigatórios
        if (List.of(2, 3, 4).contains(registerRequest.getTipo())) {
            if (registerRequest.getDelegacia() == null || registerRequest.getDistintivo() == null || registerRequest.getRa() == null) {
                throw new IllegalArgumentException("Delegacia, distintivo e RA são obrigatórios para este tipo de usuário.");
            }
            user.setDelegate(registerRequest.getDelegacia());
            user.setBadge(registerRequest.getDistintivo());
            user.setRa(registerRequest.getRa());
        }

        if (registerRequest.getTipo() == 5) {
            if (registerRequest.getDepartamento() == null || registerRequest.getCargo() == null) {
                throw new IllegalArgumentException("Departamento e cargo são obrigatórios para este tipo de usuário.");
            }
            user.setDepartamento(registerRequest.getDepartamento());
            user.setCargo(registerRequest.getCargo());
        }

        // Salva novo ou atualizado
        userRepository.save(user);

    }

    public User getUserDetails(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User  not found"));
    }

    public void forgotPassword(String emailCpf) {
            User user;

            if (emailCpf.contains("@")) {
                user = userRepository.findByEmail(emailCpf)
                        .orElseThrow(() -> new UsernameNotFoundException("Usuário com esse e-mail não encontrado"));
            } else {
                user = userRepository.findByCpf(emailCpf)
                        .orElseThrow(() -> new UsernameNotFoundException("Usuário com esse CPF não encontrado"));
            }

            // Gera token único
            String token = UUID.randomUUID().toString();

            // Salva o token com expiração (por ex: 1h)
            PasswordResetToken resetToken = new PasswordResetToken(
                    token,
                    user,
                    LocalDateTime.now().plusHours(1),
                    false // usado?
            );

            tokenRepository.save(resetToken);

        Map<String, Object> usuario = new HashMap<>();
        usuario.put("nome", user.getUsername());
        usuario.put("email", user.getEmail());
        usuario.put("cpf", user.getCpf());
        usuario.put("Permissao", user.getRole().getName());
        usuario.put("token", token);
        usuario.put("usuario", usuario);
    }

    public boolean resetarSenha(String token, String novaSenha) {
        Optional<PasswordResetToken> tokenOpt = tokenRepository.findByToken(token);

        if (tokenOpt.isEmpty()) return false;

        PasswordResetToken resetToken = tokenOpt.get();

        if (resetToken.isUsed() || resetToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            return false;
        }

        User user = resetToken.getUser();
        user.setPassword(passwordEncoder.encode(novaSenha));
        userRepository.save(user);

        resetToken.setUsed(true);
        tokenRepository.save(resetToken);
        return true;
    }

    public boolean updateUserData(String id, UserUpdateRequest userUpdateRequest) {
        User user = userRepository.findById(Long.valueOf(id)).orElseThrow(() -> new RuntimeException("Usuário não encontrado."));
        RoleType roleType = switch (userUpdateRequest.getTipo()) {
            case 1 -> RoleType.PUBLICO;
            case 2 -> RoleType.POLICIAL;
            case 3 -> RoleType.AGENTE_DE_SEGURANCA;
            case 4 -> RoleType.INVESTIGADOR;
            case 5 -> RoleType.GESTOR_DE_SEGURANCA_PUBLICA;
            default -> throw new IllegalArgumentException("Tipo de usuário inválido.");
        };
        Role role = roleRepository.findByName(roleType)
                .orElseThrow(() -> new RuntimeException("Papel não encontrado: " + roleType));
        user.setUsername(userUpdateRequest.getUsername());
        user.setEmail(userUpdateRequest.getEmail());
        user.setCpf(userUpdateRequest.getCpf());
        user.setRole(role);


        // Adicionando campos adicionais dependendo do tipo de usuário
        if (userUpdateRequest.getTipo() == 2 || userUpdateRequest.getTipo() == 3 || userUpdateRequest.getTipo() == 4) {
            user.setDelegate(userUpdateRequest.getDelegacia());
            user.setBadge(userUpdateRequest.getDistintivo());
            user.setRa(userUpdateRequest.getRa());
        }

        // Se for Gestor de Segurança Pública, mantemos os campos
        if (userUpdateRequest.getTipo() == 5) {
            user.setDepartamento(userUpdateRequest.getDepartamento());
            user.setCargo(userUpdateRequest.getCargo());
        }

        userRepository.save(user);
        return true;
    }

}
