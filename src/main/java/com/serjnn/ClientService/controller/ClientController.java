package com.serjnn.ClientService.controller;


import com.serjnn.ClientService.dtos.AuthRequest;
import com.serjnn.ClientService.dtos.RegRequest;
import com.serjnn.ClientService.models.Client;
import com.serjnn.ClientService.services.ClientDetailService;
import com.serjnn.ClientService.services.ClientService;
import com.serjnn.ClientService.services.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class ClientController {
    private final ClientService clientService;
    private final AuthenticationManager authenticationManager;
    private final ClientDetailService clientDetailService;
    private final JwtService jwtService;

    @PostMapping("/register")
    public ResponseEntity<?> reg(@RequestBody RegRequest regRequest) {

        if (regRequest.getUsername() == null || regRequest.getPassword() == null ) {
            return new ResponseEntity<>("Некоторые обязательные поля отсутствуют", HttpStatus.BAD_REQUEST);
        }
        clientService.register(regRequest);
        return ResponseEntity.ok("successful registration ");


    }


    @PostMapping("/auth")
    public ResponseEntity<?> auth(@RequestBody AuthRequest authRequest) {
        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(authRequest.getUsername(),
                    authRequest.getPassword()));
        } catch (BadCredentialsException e) {

            return new ResponseEntity<>(new Error(), HttpStatus.UNAUTHORIZED);
        }
        UserDetails userDetails = clientDetailService.loadUserByUsername(authRequest.getUsername());

        String token = jwtService.generateToken(userDetails);
        return ResponseEntity.ok(token);
    }

    @GetMapping("/some")
    String some (){

        return "some";
    }

    @PostMapping("/validate")
    public ResponseEntity<?> validateToken(@RequestHeader("Authorization") String token) {
        token = token.substring(7);

        try {
            UserDetails userDetails = clientDetailService.loadUserByUsername(jwtService.extractUsername(token));
            // Валидация токена
            boolean isValid = jwtService.isTokenValid(token,userDetails);
            if (isValid) {
                return ResponseEntity.ok().build();  // Токен валиден
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid token");
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Token validation failed");
        }
    }









}
