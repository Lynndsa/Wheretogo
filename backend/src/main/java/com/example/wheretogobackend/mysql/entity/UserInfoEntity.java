package com.example.wheretogobackend.mysql.entity;

import com.example.wheretogobackend.mysql.entity.UserEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity
@Table(name = "user_information")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserInfoEntity {

    @Id
    @Column(name = "id_user")
    private Long idUser;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "id_user")
    private UserEntity user;

    @Column(name = "name", nullable = false, length = 20)
    private String name;

    @Column(name = "lastname", length = 50)
    private String lastname;

    @Column(name = "surname", length = 50)
    private String surname;

    @Column(name = "phonenumber", nullable = false, unique = true, length = 11)
    private String phonenumber;

    @Column(name = "email", nullable = false, unique = true, length = 150)
    private String email;
}