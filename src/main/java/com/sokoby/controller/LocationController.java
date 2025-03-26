package com.sokoby.controller;
import  com.sokoby.entity.Location;
import com.sokoby.mapper.LocationMapper;
import com.sokoby.payload.LocationDto;
import com.sokoby.service.LocationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/locations")
public class LocationController {

    private final LocationService locationService;

    @Autowired
    public LocationController(LocationService locationService) {
        this.locationService = locationService;
    }

    @GetMapping
    public ResponseEntity<List<LocationDto>> getAllLocations() {
        List<LocationDto> dtos = locationService.getAllLocations().stream()
                .map(LocationMapper::toDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/{id}")
    public ResponseEntity<LocationDto> getLocationById(@PathVariable UUID id) {
        Location location = locationService.getLocationById(id);
        return ResponseEntity.ok(LocationMapper.toDto(location));
    }

    @PostMapping
    public ResponseEntity<LocationDto> createLocation(@RequestBody LocationDto dto) {
        Location location = LocationMapper.toEntity(dto);
        Location created = locationService.createLocation(location);
        return new ResponseEntity<>(LocationMapper.toDto(created), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<LocationDto> updateLocation(@PathVariable UUID id, @RequestBody LocationDto dto) {
        Location location = LocationMapper.toEntity(dto);
        location.setId(id);
        Location updated = locationService.updateLocation(location);
        return ResponseEntity.ok(LocationMapper.toDto(updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteLocation(@PathVariable UUID id) {
        locationService.deleteLocation(id);
        return ResponseEntity.noContent().build();
    }
}