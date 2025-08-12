package org.example.repositorie;

import org.example.model.Temperature;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TemperatureLogRepository extends JpaRepository<Temperature, Long> {

}
