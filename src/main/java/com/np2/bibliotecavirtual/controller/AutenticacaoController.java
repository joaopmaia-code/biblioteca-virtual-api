package com.np2.bibliotecavirtual.controller;

import com.np2.bibliotecavirtual.dto.ErroResponseDTO;
import com.np2.bibliotecavirtual.dto.LoginRequestDTO;
import com.np2.bibliotecavirtual.dto.LoginResponseDTO;
import com.np2.bibliotecavirtual.dto.TrocaSenhaRequestDTO;
import com.np2.bibliotecavirtual.dto.UsuarioResponseDTO;
import com.np2.bibliotecavirtual.seguranca.UsuarioLogado;
import com.np2.bibliotecavirtual.service.AutenticacaoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Autenticação", description = "Login com matrícula e senha (devolve o token JWT) e troca de senha")
public class AutenticacaoController {

    private final AutenticacaoService autenticacaoService;
    private final UsuarioLogado usuarioLogado;

    @PostMapping("/login")
    @SecurityRequirements // rota pública: não exige token
    @Operation(summary = "Entrar",
            description = "Devolve o token JWT. No Swagger, clique em Authorize e cole o token para usar as outras rotas.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Login feito",
                    content = @Content(schema = @Schema(implementation = LoginResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Matrícula ou senha em branco",
                    content = @Content(schema = @Schema(implementation = ErroResponseDTO.class))),
            @ApiResponse(responseCode = "401", description = "Matrícula ou senha inválidas",
                    content = @Content(schema = @Schema(implementation = ErroResponseDTO.class)))
    })
    public ResponseEntity<LoginResponseDTO> entrar(@Valid @RequestBody LoginRequestDTO dto) {
        return ResponseEntity.ok(autenticacaoService.entrar(dto));
    }

    @GetMapping("/eu")
    @Operation(summary = "Quem sou eu", description = "Dados do usuário dono do token.")
    @ApiResponse(responseCode = "200", description = "Usuário logado",
            content = @Content(schema = @Schema(implementation = UsuarioResponseDTO.class)))
    public ResponseEntity<UsuarioResponseDTO> eu() {
        return ResponseEntity.ok(autenticacaoService.dadosDe(usuarioLogado.id()));
    }

    @PutMapping("/senha")
    @Operation(summary = "Trocar a própria senha")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Senha trocada"),
            @ApiResponse(responseCode = "400", description = "Senha atual incorreta ou nova senha inválida",
                    content = @Content(schema = @Schema(implementation = ErroResponseDTO.class)))
    })
    public ResponseEntity<Void> trocarSenha(@Valid @RequestBody TrocaSenhaRequestDTO dto) {
        autenticacaoService.trocarSenha(usuarioLogado.id(), dto);
        return ResponseEntity.noContent().build();
    }
}
