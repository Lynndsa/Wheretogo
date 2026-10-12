package com.example.wheretogobackend.service;

import com.example.wheretogobackend.mysql.entity.DataEntity;
import com.example.wheretogobackend.mysql.repository.DataRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CustomPlaceService {

    private final DataRepository dataRepository;


    // получение всех сохраненныъ мест
    public List<DataEntity> getAllPlaces() {
        return dataRepository.findAll();
    }

    // поиск места по ID
    public Optional<DataEntity> getPlaceById(Long id) {
        return dataRepository.findById(id);
    }

}