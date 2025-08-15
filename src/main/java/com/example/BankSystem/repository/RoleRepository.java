package com.example.BankSystem.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.example.BankSystem.model.Role;
import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role,Long> {

    /**
     * Find a role by name
     */
    Optional<Role> findByName(String name);
}
