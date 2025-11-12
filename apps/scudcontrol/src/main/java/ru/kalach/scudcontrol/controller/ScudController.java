package ru.kalach.scudcontrol.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.kalach.scudcontrol.model.Device;
import ru.kalach.scudcontrol.model.SCUDControlDevice;
import ru.kalach.scudcontrol.service.SCUDControlService;

import java.util.List;

@RestController
@RequestMapping("/")
@RequiredArgsConstructor
public class ScudController {

    private final SCUDControlService scudControlService;

    @GetMapping("/getStatusById/{id}")
    public ResponseEntity<Boolean> getStatusById(@PathVariable String id) {
        SCUDControlDevice device = scudControlService.getScud(id);
        if (device != null) {
            return ResponseEntity.ok(device.isOpened());
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/getScudParametersById/{id}")
    public SCUDControlDevice getScudById(@PathVariable String id) {
        return scudControlService.getScud(id);
    }

    @GetMapping("/all")
    public ResponseEntity<List<SCUDControlDevice>> getAll() {
        return ResponseEntity.ok(scudControlService.getAllScuds());
    }

    @PostMapping
    public ResponseEntity<String> createScud(@RequestBody Device scud) {
        return ResponseEntity.ok(scudControlService.createScud(scud));
    }

    @DeleteMapping("/{id}")
    public void deleteScud(@PathVariable String id) {
        scudControlService.deleteScud(id);
    }

    @PutMapping("/{id}")
    public void updateScud(@RequestParam Boolean opened, @PathVariable String id) {
        scudControlService.updateScud(id, opened);
    }
}
