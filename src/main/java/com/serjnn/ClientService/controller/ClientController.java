package com.serjnn.ClientService.controller;


import com.serjnn.ClientService.dto.AuthRequest;
import com.serjnn.ClientService.dto.ClientInfoDto;
import com.serjnn.ClientService.dto.OrderDTO;
import com.serjnn.ClientService.dto.RegRequest;
import com.serjnn.ClientService.service.ClientDetailService;
import com.serjnn.ClientService.service.ClientService;
import com.serjnn.ClientService.service.JwtService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@RequestMapping("/api/v1/clients")
@Tag(name = "Client API", description = "Endpoints for managing client registration, authentication, balance, and profiles")
public class ClientController {
    private final ClientService clientService;
    private final ReactiveAuthenticationManager authenticationManager;
    private final ClientDetailService clientDetailService;
    private final JwtService jwtService;
    private static final String EMAIL_REGEX = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";

    @PostMapping("/register")
    @Operation(summary = "Register client", description = "Create a new client profile")
    @ApiResponse(responseCode = "200", description = "Client successfully registered")
    @ApiResponse(responseCode = "400", description = "Invalid email formatting or missing fields")
    public ResponseEntity<?> reg(@RequestBody RegRequest regRequest) {
        log.info("Request received: Register client: {}", regRequest);
        if (regRequest.getMail() == null || regRequest.getPassword() == null) {
            return new ResponseEntity<>("Некоторые обязательные поля отсутствуют", HttpStatus.BAD_REQUEST);
        }
        if (!regRequest.getMail().matches(EMAIL_REGEX)) {
            return new ResponseEntity<>("Mail does not match the regex", HttpStatus.BAD_REQUEST);
        }
        return ResponseEntity.ok(clientService.register(regRequest));
    }

    @GetMapping("/secured")
    @Operation(summary = "Secured test endpoint")
    public Mono<Object> som() {
        log.info("Request received: Access secured endpoint");
        return Mono.empty();
    }

    @PostMapping("/auth")
    @Operation(summary = "Authenticate client", description = "Retrieve a JWT authorization token by verifying credentials")
    @ApiResponse(responseCode = "200", description = "Authentication successful, token returned")
    @ApiResponse(responseCode = "400", description = "Invalid credentials")
    public Mono<ResponseEntity<String>> auth(@RequestBody AuthRequest authRequest) {
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
    @Operation(summary = "Validate authorization token", description = "Check if the provided JWT token is valid")
    @ApiResponse(responseCode = "200", description = "Token is valid")
    @ApiResponse(responseCode = "401", description = "Token is invalid or expired")
    public Mono<ResponseEntity<?>> validateToken(@RequestHeader("Authorization") String token) {
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

    @GetMapping("/me")
    @Operation(summary = "Get current client details", description = "Retrieve information for the currently authenticated client")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved client profile information")
    public Mono<ClientInfoDto> clientInfo() {
        log.info("Request received: Get client info");
        return clientService.getClientInfo();
    }

    @PostMapping("/{clientId}/balance/add/{amount}")
    @Operation(summary = "Add balance to client", description = "Increase the balance of a specific client by a given amount")
    @ApiResponse(responseCode = "200", description = "Balance successfully added")
    public Mono<Void> addBalance(@PathVariable("clientId") Long clientId, @PathVariable("amount") BigDecimal amount) {
        log.info("Request received: Add balance. Client: {}, Amount: {}", clientId, amount);
        return clientService.addBalance(clientId, amount);
    }

    @PutMapping("/me/address")
    @Operation(summary = "Update client address", description = "Modify the residential address of the currently authenticated client")
    @ApiResponse(responseCode = "200", description = "Address successfully updated")
    public Mono<Void> changeAddress(@RequestParam("address") String address) {
        log.info("Request received: Change address to: {}", address);
        return clientService.setAddress(address);
    }

    @PostMapping("/balance/restore")
    @Operation(summary = "Restore client balance", description = "Refund money for a cancelled order to client balance")
    @ApiResponse(responseCode = "200", description = "Balance successfully restored")
    public Mono<Void> restore(@RequestBody OrderDTO orderDTO) {
        log.info("Request received: Restore balance for order: {}", orderDTO);
        return clientService.addBalance(orderDTO.getClientID(), orderDTO.getTotalSum());
    }

    @PostMapping("/balance/deduct")
    @Operation(summary = "Deduct client balance", description = "Charge money from client balance for a completed order")
    @ApiResponse(responseCode = "200", description = "Balance successfully deducted")
    @ApiResponse(responseCode = "400", description = "Insufficient funds")
    public Mono<Void> deduct(@RequestBody OrderDTO orderDTO) {
        log.info("Request received: Deduct money for order: {}", orderDTO);
        return clientService.deductMoney(orderDTO.getClientID(), orderDTO.getTotalSum());
    }
}
