package com.serjnn.ClientService.services;

import com.serjnn.ClientService.models.Client;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service

public class ClientDetailService implements UserDetailsService {
    private ClientService clientService;


    @Autowired
    public void setClientService(@Lazy ClientService clientService) {
        this.clientService = clientService;
    }

    @Override
    public UserDetails loadUserByUsername(String mail) throws UsernameNotFoundException {
        Client client= clientService.findByMail(mail);

        return User.builder()
                .username(client.getMail())
                .password(client.getPassword())
                .roles(new String[]{client.getRole()})
                .build();
    }


}