package com.serjnn.ClientService.service;


import com.serjnn.ClientService.dto.ClientInfoDto;
import com.serjnn.ClientService.dto.RegRequest;
import com.serjnn.ClientService.model.Client;
import com.serjnn.ClientService.repository.ClientRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;

@Slf4j
@Service
@RequiredArgsConstructor
public class ClientService {
    private final ClientRepository clientRepository;
    private final PasswordEncoder passwordEncoder;

    private Mono<Client> findById(Long id) {
        return clientRepository.findById(id)
                .switchIfEmpty(Mono.error(new UsernameNotFoundException("User not found with id: " + id)));
    }

    public Mono<Void> save(Client client) {
        return clientRepository.save(client).then();
    }

    public Mono<Void> register(RegRequest regRequest) {
        log.info("Registering new client with mail: {}", regRequest.getMail());
        Client client = new Client(regRequest.getMail(),
                passwordEncoder.encode(regRequest.getPassword())
        );

        return save(client);
    }

    public Mono<Client> findByMail(String mail) {
        return clientRepository.findByMail(mail);

    }

    public Mono<Client> findCurrentClient() {
        return ReactiveSecurityContextHolder.getContext()
                .map(SecurityContext::getAuthentication)
                .map(Authentication::getName)
                .flatMap(this::findByMail);
    }

    public Mono<Void> addBalance(Long clientID, BigDecimal balance) {
        log.info("Adding balance: {} to client with ID: {}", balance, clientID);
        return findById(clientID)
                .map(client -> {
                    client.setBalance(client.getBalance().add(balance));
                    return client;
                })
                .flatMap(this::save);
    }

    public Mono<Void> setAddress(String address) {
        log.info("Setting address to: {} for current client", address);
        return findCurrentClient()
                .map(client -> {
                    client.setAddress(address);
                    return client;
                })
                .flatMap(this::save);
    }

    public Mono<Void> deductMoney(Long clientID, BigDecimal amount) {
        log.info("Deducting money: {} from client with ID: {}", amount, clientID);
        return findById(clientID)
                .flatMap(client -> {
                    if (client.getBalance().compareTo(amount) < 0) {
                        log.warn("Insufficient funds for client with ID: {}. Current balance: {}, requested amount: {}", clientID, client.getBalance(), amount);
                        return Mono.error(new IllegalArgumentException("Insufficient funds"));
                    }
                    client.setBalance(client.getBalance().subtract(amount));
                    return save(client);
                });
    }


    public Mono<ClientInfoDto> getClientInfo() {
        return findCurrentClient()
                .map(client ->
                        new ClientInfoDto(client.getId()
                                , client.getMail()
                                , client.getBalance()
                                , client.getAddress()));

    }
}
