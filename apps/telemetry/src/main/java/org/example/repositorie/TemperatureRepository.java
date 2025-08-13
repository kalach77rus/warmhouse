package org.example.repositorie;

import org.example.model.Temperature;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TemperatureRepository extends JpaRepository<Temperature, Long> {
	Optional<Temperature> findFirstByLocation(String location);

	Optional<Temperature> findFirstBySensorId(String sensorId);
}
