package kartingRM.Backend.Repositories;

import kartingRM.Backend.Entities.UserEntity;
import kartingRM.Backend.Repositories.UserRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;


@Repository
public interface UserRepository extends JpaRepository<UserEntity, Long> {
    Optional<UserEntity> findByRut(String rut);
    
    Optional<UserEntity> findById(Long id);
}
