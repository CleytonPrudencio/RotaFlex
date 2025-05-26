package br.com.rotaflex.config;

import br.com.rotaflex.entity.Role;
import br.com.rotaflex.entity.RoleType;
import br.com.rotaflex.repository.RoleRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class RoleInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;

    public RoleInitializer(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    @Override
    public void run(String... args) {
        for (RoleType roleType : RoleType.values()) {
            roleRepository.findById(roleType.getId())
                    .orElseGet(() -> {
                        Role role = new Role();
                        role.setId(roleType.getId());
                        role.setName(roleType.getNome());
                        return roleRepository.save(role);
                    });
        }
    }
}

