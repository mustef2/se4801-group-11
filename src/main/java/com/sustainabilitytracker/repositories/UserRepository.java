package com.sustainabilitytracker.repositories;

import com.sustainabilitytracker.entities.User;
import com.sustainabilitytracker.enums.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    boolean existsByEmailAndIdNot(String email, Long id);

    Long countByIsActiveTrue();

    List<User> findByCompanyId(Long companyId);

    List<User> findByDepartmentId(Long departmentId);

    List<User> findByCompanyIdAndRole(Long companyId, Role role);

    List<User> findByCompanyIdAndIsActiveTrue(Long companyId);
}