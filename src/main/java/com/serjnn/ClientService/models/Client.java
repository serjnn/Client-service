package com.serjnn.ClientService.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@NoArgsConstructor
@Entity
@Getter
@Setter
@Table(name = "client")
public class Client {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(length = 70, nullable = false, unique = true)
    private String mail;

    @Column(length = 300, nullable = false)
    private String password;

    @Column(length = 10)
    private String role;

    public Client(String mail, String password) {
        this.mail = mail;
        this.password = password;
        this.role = "client";
    }


}
