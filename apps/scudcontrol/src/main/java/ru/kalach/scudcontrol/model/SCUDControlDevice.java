package ru.kalach.scudcontrol.model;

import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

public class SCUDControlDevice extends Device {

    @Getter
    @Setter
    private boolean opened;

    public SCUDControlDevice(Device device, Instant created, Instant updated, boolean opened) {
        super(UUID.randomUUID().toString(), device.getType(), device.getLocation(), device.getStatus(), device.getRegion(), updated, created);
        this.opened = opened;
    }
}
