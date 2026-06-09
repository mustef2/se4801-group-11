package com.sustainabilitytracker.repositories;

import com.sustainabilitytracker.entities.Department;
import jakarta.validation.constraints.NotBlank;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;


public interface DepartmentRepository extends JpaRepository<Department, Long> {
    List<Department> findByCompanyId(Long companyId);
    boolean existsByNameAndCompanyId(String name, Long companyId);
    boolean existsByNameAndCompanyIdAndIdNot(@NotBlank String name, Long companyId, Long departmentId);
}
