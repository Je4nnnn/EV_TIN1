package kartingRM.Backend.repositories;

import kartingRM.Backend.BackendApplication;
import kartingRM.Backend.Entities.UserEntity;
import kartingRM.Backend.Repositories.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.test.context.ContextConfiguration;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
@ContextConfiguration(classes = BackendApplication.class)
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    void findByRut_givenPersistedUser_thenReturnsMatch() {
        UserEntity user = new UserEntity();
        user.setRut("12-3");
        user.setName("Ana");
        user.setEmail("ana@hotelrm.test");
        user.setPhoneNumber("+56922222222");
        userRepository.save(user);

        Optional<UserEntity> result = userRepository.findByRut("12-3");

        assertTrue(result.isPresent());
        assertEquals("Ana", result.get().getName());
        assertEquals("12-3", result.get().getRut());
    }
}
