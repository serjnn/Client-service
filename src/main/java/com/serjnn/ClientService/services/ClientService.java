package com.serjnn.ClientService.services;


import com.serjnn.ClientService.dtos.RegRequest;
import com.serjnn.ClientService.models.Client;
import com.serjnn.ClientService.repo.ClientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

    private Client findById(Long id) {return clientRepository.findById(id).orElseThrow(() ->
            new NoSuchElementException("NO client with that id"));}
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

    public void addBalance(Long clientID, BigDecimal balance) {
        Client client = findById(clientID);
        client.setBalance(client.getBalance().add(balance));
        save(client);
    }

    public void addBalance( BigDecimal balance) {
        Client client = findCurrentClient();
        client.setBalance(client.getBalance().add(balance));
        save(client);
    }

    public void setAddress(String address) {
        Client client = findCurrentClient();
        client.setAddress(address);
        save(client);
    }

    public ResponseEntity<HttpStatus> deductMoney(Long clientID, BigDecimal amount) {
        Client client = findById(clientID);
        if (client.getBalance().compareTo(amount) < 0) {
            return new ResponseEntity<>(HttpStatus.CONFLICT); // 409
        }
        client.setBalance(client.getBalance().subtract(amount));
        save(client);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
