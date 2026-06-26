package com.sustainabilitytracker.repositories;

import com.sustainabilitytracker.entities.Department;
import jakarta.validation.constraints.NotBlank;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DepartmentRepository extends JpaRepository<Department, Long> {

    List<Department> findByCompanyId(Long companyId);

    boolean existsByNameAndCompanyId(String name, Long companyId);

    boolean existsByNameAndCompanyIdAndIdNot(String name, Long companyId, Long departmentId);
}