package com.sokoby.service.impl;

import com.sokoby.entity.Location;
import com.sokoby.exception.MerchantException;
import com.sokoby.repository.LocationRepository;
import com.sokoby.service.LocationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class LocationServiceImpl implements LocationService {

    private final LocationRepository locationRepository;

    @Autowired
    public LocationServiceImpl(LocationRepository locationRepository) {
        this.locationRepository = locationRepository;
    }

    @Override
    public List<Location> getAllLocations() {
        return locationRepository.findAll();
    }

    @Override
    public Location getLocationById(UUID id) {
        return locationRepository.findById(id)
                .orElseThrow(() -> new MerchantException("Location not found", "LOCATION_NOT_FOUND"));
    }

    @Override
    @Transactional
    public Location createLocation(Location location) {
        if (location.getName() == null || location.getName().trim().isEmpty()) {
            throw new MerchantException("Location name cannot be null or empty", "INVALID_LOCATION_NAME");
        }
        return locationRepository.save(location);
    }

    @Override
    @Transactional
    public Location updateLocation(Location location) {
        Location existing = getLocationById(location.getId());
        if (location.getName() != null) {
            existing.setName(location.getName());
        }
        if (location.getAddress() != null) {
            existing.setAddress(location.getAddress());
        }
        return locationRepository.save(existing);
    }

    @Override
    @Transactional
    public void deleteLocation(UUID id) {
        Location location = getLocationById(id);
        locationRepository.delete(location);
    }
}