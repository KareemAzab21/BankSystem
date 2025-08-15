package com.example.BankSystem.service;

import com.example.BankSystem.exception.BadRequestException;
import com.example.BankSystem.exception.ResourceNotFoundException;
import com.example.BankSystem.model.Role;
import com.example.BankSystem.repository.RoleRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
public class RoleService {

    private final RoleRepository roleRepository;

    @Autowired
    public RoleService(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    @Transactional(readOnly = true)
    public List<Role> getAllRoles() {
        log.info("Fetching all roles");
        return roleRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Role getRoleById(Long id) {
        log.info("Fetching role with ID: {}", id);
        return roleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Role", "id", id));
    }

    @Transactional(readOnly = true)
    public Role getRoleByName(String name) {
        log.info("Fetching role with name: {}", name);
        return roleRepository.findByName(name)
                .orElseThrow(() -> new ResourceNotFoundException("Role", "name", name));
    }

    @Transactional
    public Role createRole(String name) {
        log.info("Creating new role with name: {}", name);

        if (roleRepository.findByName(name).isPresent()) {
            throw new BadRequestException("Role already exists with name: " + name);
        }

        Role role = Role.builder()
                .name(name)
                .build();

        Role savedRole = roleRepository.save(role);
        log.info("Role created successfully with ID: {}", savedRole.getId());
        return savedRole;
    }

    @Transactional
    public void deleteRole(Long id) {
        log.info("Deleting role with ID: {}", id);

        if (!roleRepository.existsById(id)) {
            throw new ResourceNotFoundException("Role", "id", id);
        }

        roleRepository.deleteById(id);
        log.info("Role deleted successfully");
    }
}
