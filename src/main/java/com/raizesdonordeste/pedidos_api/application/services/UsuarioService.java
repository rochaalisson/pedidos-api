package com.raizesdonordeste.pedidos_api.application.services;

import com.raizesdonordeste.pedidos_api.api.dto.CadastroUsuarioRequestDTO;
import com.raizesdonordeste.pedidos_api.api.dto.UsuarioResponseDTO;
import com.raizesdonordeste.pedidos_api.api.exceptions.NotFoundException;
import com.raizesdonordeste.pedidos_api.api.exceptions.RegraNegocioException;
import com.raizesdonordeste.pedidos_api.domain.models.Unidade;
import com.raizesdonordeste.pedidos_api.domain.models.Usuario;
import com.raizesdonordeste.pedidos_api.infrastructure.repositories.UnidadeRepository;
import com.raizesdonordeste.pedidos_api.infrastructure.repositories.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final UnidadeRepository unidadeRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public UsuarioResponseDTO cadastrar(CadastroUsuarioRequestDTO request) {
        if (usuarioRepository.findByEmail(request.email()).isPresent()) {
            throw new RegraNegocioException("Já existe um usuário com o e-mail informado.");
        }

        Unidade unidade = null;
        if (request.unidadeId() != null) {
            unidade = unidadeRepository.findById(request.unidadeId())
                    .orElseThrow(() -> new NotFoundException("Unidade não encontrada: " + request.unidadeId()));
        }

        Usuario usuario = Usuario.builder()
                .nome(request.nome())
                .email(request.email())
                .senha(passwordEncoder.encode(request.senha()))
                .perfil(request.perfil())
                .unidade(unidade)
                .build();

        Usuario salvo = usuarioRepository.save(usuario);
        return toResponseDTO(salvo);
    }

    public List<UsuarioResponseDTO> listar() {
        return usuarioRepository.findAll().stream()
                .map(this::toResponseDTO)
                .toList();
    }

    public UsuarioResponseDTO buscarPorId(Long id) {
        return usuarioRepository.findById(id)
                .map(this::toResponseDTO)
                .orElseThrow(() -> new NotFoundException("Usuário não encontrado: " + id));
    }

    private UsuarioResponseDTO toResponseDTO(Usuario usuario) {
        Long unidadeId = usuario.getUnidade() != null ? usuario.getUnidade().getId() : null;
        return new UsuarioResponseDTO(
                usuario.getId(),
                usuario.getNome(),
                usuario.getEmail(),
                usuario.getPerfil(),
                unidadeId
        );
    }
}
