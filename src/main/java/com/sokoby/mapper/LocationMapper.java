package com.sokoby.mapper;

import com.sokoby.entity.Location;
import com.sokoby.payload.LocationDto;

public class LocationMapper {
    private LocationMapper() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    public static LocationDto toDto(Location location) {
        if (location == null) {
            throw new IllegalArgumentException("Location entity cannot be null for mapping to DTO");
        }

        LocationDto dto = new LocationDto();
        dto.setId(location.getId());
        dto.setName(location.getName());
        dto.setAddress(location.getAddress());
        dto.setCreatedAt(location.getCreatedAt());
        return dto;
    }

    public static Location toEntity(LocationDto dto) {
        if (dto == null) {
            throw new IllegalArgumentException("LocationDto cannot be null for mapping to entity");
        }

        Location location = new Location();
        if (dto.getId() != null) {
            location.setId(dto.getId());
        }
        location.setName(dto.getName());
        location.setAddress(dto.getAddress());
        return location;
    }
}