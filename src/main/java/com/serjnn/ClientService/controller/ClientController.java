package com.serjnn.ClientService.controller;


import com.serjnn.ClientService.dto.AuthRequest;
import com.serjnn.ClientService.dto.ClientInfoDto;
import com.serjnn.ClientService.dto.OrderDTO;
import com.serjnn.ClientService.dto.RegRequest;
import com.serjnn.ClientService.service.ClientDetailService;
import com.serjnn.ClientService.service.ClientService;
import com.serjnn.ClientService.service.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;

@Slf4j
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
        log.info("Request received: Register client: {}", regRequest);
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
        log.info("Request received: Access secured endpoint");
        return Mono.empty();
    }

    @PostMapping("/auth")
    Mono<ResponseEntity<String>> auth(@RequestBody AuthRequest authRequest) {
        log.info("Request received: Authenticate client: {}", authRequest.getMail());
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
        log.info("Request received: Validate token");
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
        log.info("Request received: Get client info");
        return clientService.getClientInfo();
    }

    @GetMapping("/addBalance/{clientId}/{amount}")
    Mono<Void> addBalance(@PathVariable Long clientId, @PathVariable BigDecimal amount) {
        log.info("Request received: Add balance. Client: {}, Amount: {}", clientId, amount);
        return clientService.addBalance(clientId, amount);
    }

    @PostMapping("/changeAddress")
    Mono<Void> changeAddress(@RequestParam String address) {
        log.info("Request received: Change address to: {}", address);
        return clientService.setAddress(address);
    }

    @PostMapping("/restore")
    Mono<Void> restore(@RequestBody OrderDTO orderDTO) {
        log.info("Request received: Restore balance for order: {}", orderDTO);
        return clientService.addBalance(orderDTO.getClientID(), orderDTO.getTotalSum());
    }

    @PostMapping("/deduct")
    Mono<Void> deduct(@RequestBody OrderDTO orderDTO) {
        log.info("Request received: Deduct money for order: {}", orderDTO);
        return clientService.deductMoney(orderDTO.getClientID(), orderDTO.getTotalSum());
    }


}
