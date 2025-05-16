package br.com.rotaflex.controller;

import br.com.rotaflex.dto.request.*;
import br.com.rotaflex.dto.response.AuthResponse;
import br.com.rotaflex.entity.User;
import br.com.rotaflex.service.AuthenticationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api")
public class AuthenticationController {

    @Autowired
    private AuthenticationService authenticationService;

    // Constructor for AuthenticationManager (optional, as AuthenticationService is already autowired)
    @Autowired
    public AuthenticationController(AuthenticationManager authenticationManager) {
    }

    @Operation(summary = "Login to get JWT token", description = "Authenticate user and return JWT token")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful login, JWT token returned"),
            @ApiResponse(responseCode = "400", description = "Something went wrong"),
            @ApiResponse(responseCode = "422", description = "Invalid username/password supplied")
    })
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest loginRequest) {
        String token = authenticationService.generateToken(loginRequest.getCpf(), loginRequest.getPassword());
        return ResponseEntity.ok(new AuthResponse(token));
    }

    @Operation(summary = "Register a new user", description = "Create a new user and save it to the database")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "User  registered successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "409", description = "Username already exists")
    })
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest registerRequest) {
        authenticationService.register(registerRequest);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(Map.of("success", true, "message", "Usuário criado com sucesso"));    }

    @Operation(summary = "Register a new user", description = "Create a new user and save it to the database")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "User  registered successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "409", description = "Username already exists")
    })
    @PostMapping("/forgotPassword")
    public ResponseEntity<?> forgotPassword(@RequestBody ForgotPasswordRequest emailCpf) {
        authenticationService.forgotPassword(emailCpf.getEmailCpf());
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(Map.of("success", true, "message", "Usuário criado com sucesso"));    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> redefinirSenha(@RequestBody ResetarSenhaRequest request) {
        boolean sucesso = authenticationService.resetarSenha(request.getToken(), request.getNovaSenha());
        if (sucesso) {
            return ResponseEntity.ok("Senha redefinida com sucesso.");
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Token inválido ou expirado.");
        }
    }

    @Operation(summary = "Get user details", description = "Retrieve the details of the authenticated user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User  details retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized, invalid token")
    })
    @GetMapping("/user")
    public ResponseEntity<User> getUserDetails() {
        String username = getCurrentUsername();

        if (username == null) {
            return ResponseEntity.status(403).body(null); // Se não tiver usuário, retorna erro
        }

        // Buscar o usuário no banco de dados com o nome de usuário
        User user = authenticationService.getUserDetails(username);
        return ResponseEntity.ok(user);
    }

    @PutMapping("/usuario/update/{id}")
    public ResponseEntity<?> updateUser(
            @PathVariable String id,
            @RequestBody UserUpdateRequest userUpdateRequest) {

        try {
            authenticationService.updateUserData(id, userUpdateRequest);
            return ResponseEntity.ok("Usuário atualizado com sucesso.");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Erro ao atualizar os dados do usuário.");
        }
    }

    private String getCurrentUsername() {
        // Obtém a autenticação atual do SecurityContextHolder
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (principal instanceof UserDetails) {
            return ((UserDetails) principal).getUsername();
        } else {
            return null;
        }
    }
}
