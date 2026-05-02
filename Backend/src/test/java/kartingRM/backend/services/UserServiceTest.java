package kartingRM.Backend.services;

import kartingRM.Backend.Entities.UserEntity;
import kartingRM.Backend.Exceptions.BusinessException;
import kartingRM.Backend.Exceptions.ResourceNotFoundException;
import kartingRM.Backend.Repositories.UserRepository;
import kartingRM.Backend.Services.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    @Test
    void updateNumberVisits_givenPositiveVisits_whenUpdated_thenPersistsVisitsAndCategory() {
        // GIVEN
        UserEntity user = buildUser(1L, 1, "No frecuente");
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(UserEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // WHEN
        UserEntity updated = userService.updateNumberVisits(1L, 7);

        // THEN
        assertEquals(7, updated.getNumberVisits());
        assertEquals("Muy frecuente", updated.getCategory_frecuency());
    }

    @Test
    void updateNumberVisits_givenNegativeVisits_whenUpdated_thenStoresZero() {
        // GIVEN
        UserEntity user = buildUser(1L, 2, "Regular");
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(UserEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // WHEN
        UserEntity updated = userService.updateNumberVisits(1L, -3);

        // THEN
        assertEquals(0, updated.getNumberVisits());
        assertEquals("No frecuente", updated.getCategory_frecuency());
    }

    @Test
    void updateNumberVisits_givenUserWithNegativePersistedVisits_whenUpdated_thenNormalizesBeforeApplying() {
        // GIVEN
        UserEntity user = buildUser(1L, -5, "");
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(UserEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // WHEN
        UserEntity updated = userService.updateNumberVisits(1L, 3);

        // THEN
        assertEquals(3, updated.getNumberVisits());
        assertEquals("Regular", updated.getCategory_frecuency());
    }

    @Test
    void updateNumberVisits_givenUnknownUser_whenUpdated_thenThrowsResourceNotFoundException() {
        // GIVEN
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        // WHEN
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> userService.updateNumberVisits(999L, 3));

        // THEN
        assertEquals("Usuario no encontrado con ID: 999", exception.getMessage());
    }

    @Test
    void updateNumberVisits_givenBoundaryVisitsFive_whenUpdated_thenAssignsFrecuenteCategory() {
        // GIVEN
        UserEntity user = buildUser(1L, 0, "No frecuente");
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(UserEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // WHEN
        UserEntity updated = userService.updateNumberVisits(1L, 5);

        // THEN
        assertEquals("Frecuente", updated.getCategory_frecuency());
    }

    @Test
    void obtenerDescuentoPorCategoria_givenMuyFrecuenteUser_whenRequested_thenReturnsTwentyPercent() {
        // GIVEN
        UserEntity user = buildUser(1L, 7, "Muy frecuente");
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        // WHEN
        double discount = userService.obtenerDescuentoPorCategoria(1L);

        // THEN
        assertEquals(0.20, discount);
    }

    @Test
    void obtenerDescuentoPorCategoria_givenFrecuenteUser_whenRequested_thenReturnsTenPercent() {
        // GIVEN
        UserEntity user = buildUser(1L, 5, "Frecuente");
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        // WHEN
        double discount = userService.obtenerDescuentoPorCategoria(1L);

        // THEN
        assertEquals(0.10, discount);
    }

    @Test
    void obtenerDescuentoPorCategoria_givenRegularUser_whenRequested_thenReturnsFivePercent() {
        // GIVEN
        UserEntity user = buildUser(1L, 3, "Regular");
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        // WHEN
        double discount = userService.obtenerDescuentoPorCategoria(1L);

        // THEN
        assertEquals(0.05, discount);
    }

    @Test
    void obtenerDescuentoPorCategoria_givenNegativeVisitsUser_whenRequested_thenNormalizesToNoDiscount() {
        // GIVEN
        UserEntity user = buildUser(1L, -2, "");
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(UserEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // WHEN
        double discount = userService.obtenerDescuentoPorCategoria(1L);

        // THEN
        assertEquals(0.0, discount);
    }

    @Test
    void obtenerDescuentoPorCategoria_givenUnknownUser_whenRequested_thenThrowsResourceNotFoundException() {
        // GIVEN
        when(userRepository.findById(404L)).thenReturn(Optional.empty());

        // WHEN
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> userService.obtenerDescuentoPorCategoria(404L));

        // THEN
        assertEquals("Usuario no encontrado con ID: 404", exception.getMessage());
    }

    @Test
    void saveUser_givenInvalidRut_whenSaved_thenThrowsBusinessException() {
        // GIVEN
        UserEntity user = buildUser(1L, 0, "No frecuente");
        user.setRut("11111111-2");
        user.setDateBirthday(LocalDate.of(1992, 5, 10));

        // WHEN
        BusinessException exception = assertThrows(BusinessException.class, () -> userService.saveUser(user));

        // THEN
        assertEquals("El RUT ingresado no es valido.", exception.getMessage());
    }

    @Test
    void saveUser_givenFutureBirthday_whenSaved_thenThrowsBusinessException() {
        // GIVEN
        UserEntity user = buildUser(1L, 0, "No frecuente");
        user.setRut("11111111-1");
        user.setDateBirthday(LocalDate.now().plusDays(1));

        // WHEN
        BusinessException exception = assertThrows(BusinessException.class, () -> userService.saveUser(user));

        // THEN
        assertEquals("La fecha de nacimiento no puede estar en el futuro.", exception.getMessage());
    }

    @Test
    void saveUser_givenAgeOverOneHundred_whenSaved_thenThrowsBusinessException() {
        // GIVEN
        UserEntity user = buildUser(1L, 0, "No frecuente");
        user.setRut("11111111-1");
        user.setDateBirthday(LocalDate.now().minusYears(101));

        // WHEN
        BusinessException exception = assertThrows(BusinessException.class, () -> userService.saveUser(user));

        // THEN
        assertEquals("No se permiten usuarios con mas de 100 anos.", exception.getMessage());
    }

    @Test
    void saveUser_givenValidRutWithFormatting_whenSaved_thenNormalizesAndPersists() {
        // GIVEN
        UserEntity user = buildUser(1L, 0, "No frecuente");
        user.setRut("11.111.111-1");
        user.setDateBirthday(LocalDate.of(1992, 5, 10));
        when(userRepository.findByRut("111111111")).thenReturn(Optional.empty());
        when(userRepository.save(any(UserEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // WHEN
        UserEntity saved = userService.saveUser(user);

        // THEN
        assertEquals("111111111", saved.getRut());
        assertEquals(LocalDate.of(1992, 5, 10), saved.getDateBirthday());
        verify(userRepository).save(any(UserEntity.class));
    }

    @Test
    void isValidRut_givenVerifierK_whenChecked_thenReturnsTrue() {
        assertTrue(userService.isValidRut("12.426.095-7"));
    }

    private UserEntity buildUser(Long id, Integer visits, String category) {
        UserEntity user = new UserEntity();
        user.setId(id);
        user.setRut("11-1");
        user.setName("Usuario");
        user.setNumberVisits(visits);
        user.setCategory_frecuency(category);
        user.setDateBirthday(LocalDate.of(1990, 1, 1));
        return user;
    }
}
