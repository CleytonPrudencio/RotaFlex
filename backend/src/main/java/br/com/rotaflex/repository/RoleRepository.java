package br.com.rotaflex.repository;

import br.com.rotaflex.entity.Role;
import br.com.rotaflex.entity.RoleType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByName(String name);
    Optional<Role> findByName(RoleType name);

}
