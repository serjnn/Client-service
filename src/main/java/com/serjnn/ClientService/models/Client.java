package com.serjnn.ClientService.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.Collection;
import java.util.Collections;


@NoArgsConstructor
@Entity
@Data
@Table(name = "client")
public class Client  {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(length = 70, nullable = false)
    private String username;

    @Column(length = 70, nullable = true)
    private String mail;

    @Column(length = 300, nullable = false)
    private String password;

    @Column(length = 10, nullable = true)
    private String role;

    public Client(String username,String password) {
        this.username = username;
        this.password = password;
    }


}
