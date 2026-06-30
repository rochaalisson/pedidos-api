package com.raizesdonordeste.pedidos_api.infrastructure.config;

import com.raizesdonordeste.pedidos_api.domain.enums.PerfilUsuario;
import com.raizesdonordeste.pedidos_api.domain.models.Usuario;
import com.raizesdonordeste.pedidos_api.infrastructure.repositories.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (usuarioRepository.count() == 0) {
            Usuario admin = Usuario.builder()
                    .nome("Administrador")
                    .email("admin@raizesdonordeste.com")
                    .senha(passwordEncoder.encode("admin123"))
                    .perfil(PerfilUsuario.ADMIN)
                    .build();
            usuarioRepository.save(admin);
        }
    }
}
