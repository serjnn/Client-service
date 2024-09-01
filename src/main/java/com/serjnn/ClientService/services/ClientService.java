package com.serjnn.ClientService.services;


import com.serjnn.ClientService.dtos.RegRequest;
import com.serjnn.ClientService.models.Client;
import com.serjnn.ClientService.repo.ClientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class ClientService {
    private final ClientRepository clientRepository;
    private  final PasswordEncoder passwordEncoder;


    public void save(Client client) {
        clientRepository.save(client);
    }

    public void register(RegRequest regRequest) {


        Client client = new Client(regRequest.getUsername(),
                passwordEncoder.encode(regRequest.getPassword())
                );
        client.setRole("client");

        save(client);

    }

    public Client findByUsername(String username) {
        return clientRepository.findByUsername(username).orElseThrow(() -> new NoSuchElementException("no such" +
                " client with username  " + username));

    }
}
