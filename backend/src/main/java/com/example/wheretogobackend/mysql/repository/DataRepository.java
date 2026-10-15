package com.example.wheretogobackend.mysql.repository;

import com.example.wheretogobackend.mysql.entity.DataEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DataRepository extends JpaRepository<DataEntity, Long> {

}