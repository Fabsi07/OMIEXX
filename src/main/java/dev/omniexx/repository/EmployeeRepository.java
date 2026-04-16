package dev.omniexx.repository;

import dev.omniexx.entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    List<Employee> findByCompanyIdAndActiveTrue(Long companyId);
    long countByCompanyIdAndActiveTrue(Long companyId);
}
