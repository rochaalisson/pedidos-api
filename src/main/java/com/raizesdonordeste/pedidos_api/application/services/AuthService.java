package com.raizesdonordeste.pedidos_api.application.services;

import com.raizesdonordeste.pedidos_api.api.dto.LoginRequestDTO;
import com.raizesdonordeste.pedidos_api.api.dto.LoginResponseDTO;
import com.raizesdonordeste.pedidos_api.infrastructure.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public LoginResponseDTO autenticar(LoginRequestDTO request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.senha()));

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String token = jwtService.gerarToken(userDetails);

        return new LoginResponseDTO(token, "Bearer", userDetails.getUsername());
    }
}
