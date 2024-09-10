package com.serjnn.ClientService.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;


@NoArgsConstructor
@Entity
@Getter
@Setter
@Table(name = "client")
public class Client implements UserDetails {
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

    public Client(String username, String password) {
        this.username = username;
        this.password = password;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_".concat(this.role)));

    }


    @Override
    public String getUsername() {
        return this.username;
    }

}
