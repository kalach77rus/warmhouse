package ru.kalach.scudcontrol.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.kalach.scudcontrol.model.Device;
import ru.kalach.scudcontrol.model.SCUDControlDevice;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class SCUDControlService {

    private Map<String, SCUDControlDevice> devices = new HashMap<>();

    public String createScud(Device device) {
        SCUDControlDevice scudControlDevice = new SCUDControlDevice(
                device,
                Instant.now(),
                Instant.now(),
                false
        );

        devices.put(scudControlDevice.getId(), scudControlDevice);
        return scudControlDevice.getId();
    }

    public void deleteScud(String id) {
        devices.remove(id);
    }

    public void updateScud(String id, boolean opened) {
        SCUDControlDevice scudControlDevice = devices.get(id);
        if (scudControlDevice != null) {
            scudControlDevice.setOpened(opened);
        }
    }

    public SCUDControlDevice getScud(String id) {
        return devices.get(id);
    }

    public List<SCUDControlDevice> getAllScuds() {
        return new ArrayList<>(devices.values());
    }
}
