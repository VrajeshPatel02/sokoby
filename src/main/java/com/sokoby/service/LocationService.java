package com.sokoby.service;

import com.sokoby.entity.Location;
import java.util.List;
import java.util.UUID;

public interface LocationService {
    List<Location> getAllLocations();
    Location getLocationById(UUID id);
    Location createLocation(Location location);
    Location updateLocation(Location location);
    void deleteLocation(UUID id);
}