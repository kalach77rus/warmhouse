package org.example.repositorie;

import org.example.model.Temperature;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Sort;
import org.springframework.lang.NonNull;

import java.util.List;
import java.util.Optional;

public interface TemperatureRepository extends JpaRepository<Temperature, Long> {

	Optional<Temperature> findFirstByLocation(String location);

	Optional<Temperature> findFirstBySensorId(String sensorId);

	@NonNull
	List<Temperature> findAll(@NonNull Sort sort);
}
