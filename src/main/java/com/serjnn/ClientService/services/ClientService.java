package com.serjnn.ClientService.services;


import com.serjnn.ClientService.dtos.RegRequest;
import com.serjnn.ClientService.models.Client;
import com.serjnn.ClientService.repo.ClientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class ClientService {
    private final ClientRepository clientRepository;
    private final PasswordEncoder passwordEncoder;


    public void save(Client client) {
        clientRepository.save(client);
    }

    public void register(RegRequest regRequest) {


        Client client = new Client(regRequest.getMail(),
                passwordEncoder.encode(regRequest.getPassword())
        );


        save(client);

    }

    public Client findByMail(String mail) {
        return clientRepository.findByMail(mail).orElseThrow(() -> new NoSuchElementException("no such" +
                " client with mail  " + mail));

    }

    public Client findCurrentClient() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentMail = authentication.getName();
        return findByMail(currentMail);

    }

    public void addBalance(BigDecimal balance) {
        Client client = findCurrentClient();
        client.setBalance(client.getBalance().add(balance));
        save(client);
    }

    public void setAddress(String address) {
        Client client = findCurrentClient();
        client.setAddress(address);
        save(client);
    }
}
