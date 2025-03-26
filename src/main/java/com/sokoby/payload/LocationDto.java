package com.sokoby.payload;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;
import java.util.UUID;

@Getter
@Setter
public class LocationDto {
    private UUID id;
    private String name;
    private String address;
    private Date createdAt;
}