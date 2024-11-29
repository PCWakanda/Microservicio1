package org.example.microservicio1;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CarreteraRepository extends JpaRepository<Carretera, String> {
}