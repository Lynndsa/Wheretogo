
package com.example.wheretogobackend.mysql.repository;

import com.example.wheretogobackend.mysql.entity.AttractionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AttractionMySqlRepository
        extends JpaRepository<AttractionEntity, Integer> {

    boolean existsByName(String name);
}
