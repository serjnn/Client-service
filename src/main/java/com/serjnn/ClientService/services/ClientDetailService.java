package com.serjnn.ClientService.services;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service

public class ClientDetailService implements UserDetailsService {
    private ClientService clientService;

    @Autowired
    public void setClientService(@Lazy ClientService clientService) {
        this.clientService = clientService;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {


        return clientService.findByUsername(username);
    }


}