
package com.example.wheretogobackend.service;

import com.example.wheretogobackend.dto.AttractionDto;
import com.example.wheretogobackend.mysql.entity.DataEntity;
import com.example.wheretogobackend.mysql.repository.DataRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class AttractionService {

    private final DataRepository dataRepository;

}

