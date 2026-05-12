package br.com.remediar.domain.repository;

import br.com.remediar.domain.model.AppUser;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AppUserRepository extends JpaRepository<AppUser, Long> {

    Optional<AppUser> findByCpf(String cpf);

    Optional<AppUser> findByUsername(String username);
}
