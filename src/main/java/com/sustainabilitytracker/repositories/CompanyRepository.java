package com.sustainabilitytracker.repositories;

import com.sustainabilitytracker.entities.Company;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface CompanyRepository extends JpaRepository<Company, Long> {

    boolean existsByName(String name);

    boolean existsByNameAndIdNot(String name, Long id);

    Optional<Company> findByIdAndIsActiveTrue(Long companyId);

    Long countByIsActiveTrue();

    List<Company> findAllByIsActiveTrue();
}