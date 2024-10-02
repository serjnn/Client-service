package com.serjnn.ClientService.controller;


import com.serjnn.ClientService.dtos.AuthRequest;
import com.serjnn.ClientService.dtos.ClientInfoDto;
import com.serjnn.ClientService.dtos.OrderDTO;
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
import reactor.core.publisher.Mono;

import java.math.BigDecimal;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class ClientController {
    private final ClientService clientService;
    private final AuthenticationManager authenticationManager;
    private final ClientDetailService clientDetailService;
    private final JwtService jwtService;
    private static final String EMAIL_REGEX = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";


    @PostMapping("/register")
    public ResponseEntity<?> reg(@RequestBody RegRequest regRequest) {

        if (regRequest.getMail() == null || regRequest.getPassword() == null) {
            return new ResponseEntity<>("Некоторые обязательные поля отсутствуют", HttpStatus.BAD_REQUEST);
        }
        if (!regRequest.getMail().matches(EMAIL_REGEX)) {
            return new ResponseEntity<>("Mail does not math the regex", HttpStatus.BAD_REQUEST);
        }
        clientService.register(regRequest);
        return ResponseEntity.ok("successful registration ");


    }


    @PostMapping("/auth")
    public ResponseEntity<?> auth(@RequestBody AuthRequest authRequest) {
        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(authRequest.getMail(),
                    authRequest.getPassword()));
        } catch (BadCredentialsException e) {

            return new ResponseEntity<>(new Error(), HttpStatus.UNAUTHORIZED);
        }
        UserDetails userDetails = clientDetailService.findByUsername(authRequest.getMail()).block();

        String token = jwtService.generateToken(userDetails);
        return ResponseEntity.ok(token);
    }


    @PostMapping("/validate")
    public ResponseEntity<?> validateToken(@RequestHeader("Authorization") String token) {
        token = token.substring(7);

        try {
            //TODO try this without block()
            UserDetails userDetails = clientDetailService.findByUsername(jwtService.extractUsername(token)).block();
            boolean isValid = jwtService.isTokenValid(token,userDetails);
            if (isValid) {
                return ResponseEntity.ok().build();
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid token");
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Token validation failed");
        }
    }


    @GetMapping("/myInfo")
    public Mono<ClientInfoDto> clientInfo() {
        return clientService.getClientInfo();
    }

    @PostMapping("/addBalance")
    public Mono<Void> addBalance(@RequestParam BigDecimal amount) {
        return clientService.addBalance(1L,amount);

    }

    @PostMapping("/changeAddress")
    public Mono<Void> changeAddress(@RequestParam String address) {
        return clientService.setAddress(address);

    }

    @PostMapping("/restore")
    public Mono<Void> restore(@RequestBody OrderDTO orderDTO) {
        return clientService.addBalance(orderDTO.getClientID(), orderDTO.getTotalSum());

    }
    @PostMapping("/deduct")
    public Mono<Void> deduct(@RequestBody OrderDTO orderDTO) {

       return  clientService.deductMoney(orderDTO.getClientID(),orderDTO.getTotalSum());

    }


}
