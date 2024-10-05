package com.serjnn.ClientService.controller;


import com.serjnn.ClientService.dtos.AuthRequest;
import com.serjnn.ClientService.dtos.ClientInfoDto;
import com.serjnn.ClientService.dtos.OrderDTO;
import com.serjnn.ClientService.dtos.RegRequest;
import com.serjnn.ClientService.services.ClientDetailService;
import com.serjnn.ClientService.services.ClientService;
import com.serjnn.ClientService.services.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class ClientController {
    private final ClientService clientService;
    private final ReactiveAuthenticationManager authenticationManager;
    private final ClientDetailService clientDetailService;
    private final JwtService jwtService;
    private static final String EMAIL_REGEX = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";


    @PostMapping("/register")
    ResponseEntity<?> reg(@RequestBody RegRequest regRequest) {

        if (regRequest.getMail() == null || regRequest.getPassword() == null) {
            return new ResponseEntity<>("Некоторые обязательные поля отсутствуют", HttpStatus.BAD_REQUEST);
        }
        if (!regRequest.getMail().matches(EMAIL_REGEX)) {
            return new ResponseEntity<>("Mail does not math the regex", HttpStatus.BAD_REQUEST);
        }
        return ResponseEntity.ok(clientService.register(regRequest));


    }

    @GetMapping("/secured")
    Mono<Object> som() {
        return Mono.empty();
    }


    @PostMapping("/auth")
    Mono<ResponseEntity<String>> auth(@RequestBody AuthRequest authRequest) {
        return authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(authRequest.getMail(),
                        authRequest.getPassword()))
                .flatMap(authentication -> clientDetailService.findByUsername(authRequest.getMail()))
                .flatMap(userDetails -> {
                    if (userDetails == null) {
                        return Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid credentials"));
                    }
                    String token = jwtService.generateToken(userDetails);
                    return Mono.just(ResponseEntity.ok(token));
                })
                .onErrorResume(BadCredentialsException.class, e -> Mono.just(ResponseEntity.badRequest().build()));
    }


    @PostMapping("/validate")
    Mono<ResponseEntity<?>> validateToken(@RequestHeader("Authorization") String token) {
        String extractedToken = token.substring(7);


        String username = jwtService.extractUsername(token);

        return clientDetailService.findByUsername(username)
                .flatMap(userDetails -> {
                    if (userDetails == null) {
                        return Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid token"));
                    }
                    boolean isValid = jwtService.isTokenValid(extractedToken, userDetails);
                    if (isValid) {
                        return Mono.just(ResponseEntity.ok().build());
                    } else {
                        return Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid token"));
                    }
                })
                .onErrorReturn(Exception.class, ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Token validation failed"));
    }


    @GetMapping("/myInfo")
    Mono<ClientInfoDto> clientInfo() {
        return clientService.getClientInfo();
    }

    @GetMapping("/addBalance/{clientId}/{amount}")
    Mono<Void> addBalance(@PathVariable Long clientId, @PathVariable BigDecimal amount) {
        return clientService.addBalance(clientId, amount);

    }

    @PostMapping("/changeAddress")
    Mono<Void> changeAddress(@RequestParam String address) {
        return clientService.setAddress(address);

    }

    @PostMapping("/restore")
    Mono<Void> restore(@RequestBody OrderDTO orderDTO) {
        return clientService.addBalance(orderDTO.getClientID(), orderDTO.getTotalSum());

    }

    @PostMapping("/deduct")
    Mono<Void> deduct(@RequestBody OrderDTO orderDTO) {
        System.out.println(orderDTO);
        return clientService.deductMoney(orderDTO.getClientID(), orderDTO.getTotalSum());

    }


}
