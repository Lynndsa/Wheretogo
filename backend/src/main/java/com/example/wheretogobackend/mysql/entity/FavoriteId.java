
package com.example.wheretogobackend.mysql.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class FavoriteId implements Serializable {

    private static final long serialVersionUID = 1L;

    @Column(name = "id_user")
    private Long idUser;

    @Column(name = "id_attraction")
    private Long idAttraction;

    public FavoriteId() {}

    public FavoriteId(Long idUser, Long idAttraction) {
        this.idUser = idUser;
        this.idAttraction = idAttraction;
    }

    public Long getIdUser() {
        return idUser;
    }

    public Long getIdAttraction() {
        return idAttraction;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FavoriteId that)) return false;
        return Objects.equals(idUser, that.idUser)
                && Objects.equals(idAttraction, that.idAttraction);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idUser, idAttraction);
    }
}
