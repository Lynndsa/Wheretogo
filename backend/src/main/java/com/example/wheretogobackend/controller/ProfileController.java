
package com.example.wheretogobackend.controller;

import com.example.wheretogobackend.dto.ProfileResponse;
import com.example.wheretogobackend.dto.ProfileUpdateRequest;
import com.example.wheretogobackend.service.ProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/profile")
@RequiredArgsConstructor
public class ProfileController {

    private final ProfileService profileService;

    // Получение профиля по ID
    @GetMapping("/{idUser}")
    public ResponseEntity<ProfileResponse> getProfile(
            @PathVariable Long idUser
    ) {
        return ResponseEntity.ok(
                profileService.getProfile(idUser)
        );
    }

    // Обновление профиля по ID
    @PutMapping("/{idUser}")
    public ResponseEntity<ProfileResponse> updateProfile(
            @PathVariable Long idUser,
            @RequestBody ProfileUpdateRequest request
    ) {
        return ResponseEntity.ok(
                profileService.updateProfile(idUser, request)
        );
    }
}
