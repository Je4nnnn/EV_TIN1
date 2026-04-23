package kartingRM.Backend.repositories;

import kartingRM.Backend.Entities.UserEntity;
import kartingRM.Backend.Repositories.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    void findByRut_givenExistingUser_returnsUser() {
        UserEntity user = new UserEntity();
        user.setRut("11-1");
        user.setName("Ana");
        userRepository.save(user);

        Optional<UserEntity> found = userRepository.findByRut("11-1");

        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Ana");
    }
}
