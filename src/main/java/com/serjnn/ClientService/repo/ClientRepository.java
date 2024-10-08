package com.serjnn.ClientService.repo;

import com.serjnn.ClientService.models.Client;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

public interface ClientRepository extends ReactiveCrudRepository<Client, Long> {
    Mono<Client> findByMail(String mail);


}
