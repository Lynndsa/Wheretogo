//package com.example.wheretogobackend.controller;
//
//import lombok.RequiredArgsConstructor;
//import org.springframework.web.bind.annotation.*;
//
//import java.util.List;
//
//@RestController
//@RequestMapping("/api/v1/places")
//@RequiredArgsConstructor
//public class PlaceController {
//
//    private final KudaGoService kudaGoService;
//
//    @GetMapping("/import")
//    public List<KudaGoPlaceDto> importPlaces(
//            @RequestParam(defaultValue = "1") int page,
//            @RequestParam(defaultValue = "10") int pageSize
//    ) {
//        return kudaGoService.fetchPlacesFromSaintPetersburg(
//                page,
//                pageSize
//        );
//    }
//}