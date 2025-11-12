package ru.kalach.scudcontrol.model;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.Instant;

@Data
@AllArgsConstructor
public class Device {

    private String id;

    private String type;

    private String location;

    private String status;

    private Regions region;

    private Instant lastUpdate;

    private Instant created;
}
