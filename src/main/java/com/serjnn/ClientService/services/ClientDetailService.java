package com.serjnn.ClientService.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service

public class ClientDetailService implements ReactiveUserDetailsService {
    private ClientService clientService;


    @Autowired
    public void setClientService(ClientService clientService) {
        this.clientService = clientService;
    }


    @Override
    public Mono<UserDetails> findByUsername(String mail) {
        return clientService.findByMail(mail)
                .switchIfEmpty(Mono.error(new UsernameNotFoundException("User not found with mail: " + mail)))
                .map(client -> User.builder()
                        .username(client.getMail())
                        .password(client.getPassword())
                        .roles(new String[]{client.getRole()})
                        .build());


    }
}