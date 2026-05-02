package kartingRM.Backend.Services;

import kartingRM.Backend.Entities.UserEntity;
import kartingRM.Backend.Exceptions.BusinessException;
import kartingRM.Backend.Exceptions.ResourceNotFoundException;
import kartingRM.Backend.Repositories.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceCoverageTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    @Test
    void getAllUsers_returnsRepositoryUsers() {
        when(userRepository.findAll()).thenReturn(List.of(user(1L, "111111111", "Maria", 1, "No frecuente")));

        List<UserEntity> users = userService.getAllUsers();

        assertEquals(1, users.size());
    }

    @Test
    void findUserByRut_givenBlankRut_returnsEmpty() {
        assertEquals(Optional.empty(), userService.findUserByRut(" "));
    }

    @Test
    void findUserByRut_givenUserWithInvalidVisitState_normalizesAndPersists() {
        UserEntity user = user(1L, "222222222", "Pedro", -3, "");
        when(userRepository.findByRut("222222222")).thenReturn(Optional.of(user));
        when(userRepository.save(any(UserEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Optional<UserEntity> result = userService.findUserByRut("22.222.222-2");

        assertEquals(0, result.orElseThrow().getNumberVisits());
        assertEquals("No frecuente", result.orElseThrow().getCategory_frecuency());
    }

    @Test
    void incrementVisits_givenExistingUser_incrementsAndUpdatesCategory() {
        UserEntity user = user(2L, "333333333", "Sofia", 4, "Regular");
        when(userRepository.findByRut("333333333")).thenReturn(Optional.of(user));
        when(userRepository.save(any(UserEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserEntity result = userService.incrementVisits("33.333.333-3");

        assertEquals(5, result.getNumberVisits());
        assertEquals("Frecuente", result.getCategory_frecuency());
    }

    @Test
    void incrementVisits_givenUnknownRut_throwsResourceNotFound() {
        when(userRepository.findByRut("999")).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> userService.incrementVisits("99-9"));

        assertEquals("Usuario no encontrado con RUT: 999", exception.getMessage());
    }

    @Test
    void saveUser_trimsAndUppercasesValues() {
        UserEntity payload = user(null, " 11.111.111-1 ", " Ana ", 0, "");
        payload.setEmail(" ana@test.com ");
        payload.setPhoneNumber(" +56912345678 ");
        when(userRepository.findByRut("111111111")).thenReturn(Optional.empty());
        when(userRepository.save(any(UserEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserEntity result = userService.saveUser(payload);

        assertEquals("111111111", result.getRut());
        assertEquals("Ana", result.getName());
        assertEquals("ana@test.com", result.getEmail());
        assertEquals("+56912345678", result.getPhoneNumber());
        assertEquals("No frecuente", result.getCategory_frecuency());
    }

    @Test
    void saveUser_givenInvalidEmail_throwsBusinessException() {
        UserEntity payload = user(null, "111111111", "Ana", 0, "No frecuente");
        payload.setEmail("correo-invalido");

        BusinessException exception = assertThrows(BusinessException.class, () -> userService.saveUser(payload));

        assertEquals("El correo del usuario no tiene un formato valido.", exception.getMessage());
    }

    @Test
    void getUserByRut_givenBlankRut_returnsNull() {
        assertNull(userService.getUserByRut(" "));
    }

    @Test
    void getUserByRut_givenExistingUser_returnsNormalizedUser() {
        UserEntity user = user(3L, "444444444", "Luis", -1, "");
        when(userRepository.findByRut("444444444")).thenReturn(Optional.of(user));
        when(userRepository.save(any(UserEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserEntity result = userService.getUserByRut("44.444.444-4");

        assertEquals(0, result.getNumberVisits());
        assertEquals("No frecuente", result.getCategory_frecuency());
    }

    @Test
    void obtenerCategoriaCliente_givenNullUserId_throwsIllegalArgumentException() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> userService.obtenerCategoriaCliente(null));

        assertEquals("El ID del usuario no puede ser nulo.", exception.getMessage());
    }

    @Test
    void obtenerCategoriaCliente_givenExistingUser_returnsDerivedCategory() {
        when(userRepository.findById(4L)).thenReturn(Optional.of(user(4L, "555555555", "Eva", 7, "Muy frecuente")));

        String category = userService.obtenerCategoriaCliente(4L);

        assertEquals("Muy frecuente", category);
    }

    @Test
    void updateUser_updatesSelectedFieldsAndTrimsValues() {
        UserEntity existing = user(5L, "666666666", "Mario", 2, "Regular");
        existing.setEmail("mario@test.com");
        existing.setPhoneNumber("+56911111111");
        when(userRepository.findById(5L)).thenReturn(Optional.of(existing));
        when(userRepository.save(any(UserEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserEntity payload = new UserEntity();
        payload.setName(" Mario Soto ");
        payload.setEmail(" mario.soto@test.com ");
        payload.setPhoneNumber(" +56999999999 ");
        payload.setDateBirthday(LocalDate.of(1990, 1, 15));

        UserEntity result = userService.updateUser(5L, payload);

        assertEquals("Mario Soto", result.getName());
        assertEquals("mario.soto@test.com", result.getEmail());
        assertEquals("+56999999999", result.getPhoneNumber());
        assertEquals(LocalDate.of(1990, 1, 15), result.getDateBirthday());
    }

    @Test
    void updateCategoryFrequency_givenExistingUser_recalculatesCategory() {
        UserEntity existing = user(6L, "777777777", "Rosa", 5, "Regular");
        when(userRepository.findById(6L)).thenReturn(Optional.of(existing));
        when(userRepository.save(any(UserEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserEntity result = userService.updateCategoryFrequency(6L);

        assertEquals("Frecuente", result.getCategory_frecuency());
    }

    @Test
    void updateNumberVisits_givenNegativeValue_storesZeroAndNoFrequentCategory() {
        UserEntity existing = user(7L, "888888888", "Tomas", 3, "Regular");
        when(userRepository.findById(7L)).thenReturn(Optional.of(existing));
        when(userRepository.save(any(UserEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserEntity result = userService.updateNumberVisits(7L, -4);

        assertEquals(0, result.getNumberVisits());
        assertEquals("No frecuente", result.getCategory_frecuency());
    }

    @Test
    void incrementVisitsAndUpdateCategory_givenNullVisits_startsFromOne() {
        UserEntity existing = user(8L, "999999999", "Laura", null, "");
        when(userRepository.findById(8L)).thenReturn(Optional.of(existing));
        when(userRepository.save(any(UserEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserEntity result = userService.incrementVisitsAndUpdateCategory(8L);

        assertEquals(1, result.getNumberVisits());
        assertEquals("No frecuente", result.getCategory_frecuency());
    }

    @Test
    void saveUser_givenDuplicateRut_throwsBusinessException() {
        UserEntity payload = user(null, "111111111", "Ana", 0, "No frecuente");
        when(userRepository.findByRut("111111111")).thenReturn(Optional.of(user(1L, "111111111", "Ana", 0, "No frecuente")));

        BusinessException exception = assertThrows(BusinessException.class, () -> userService.saveUser(payload));

        assertEquals("Ya existe un usuario registrado con ese RUT.", exception.getMessage());
    }

    @Test
    void saveUser_givenDuplicateEmail_throwsBusinessException() {
        UserEntity payload = user(null, "111111111", "Ana", 0, "No frecuente");
        payload.setEmail("ana@test.com");
        when(userRepository.findByRut("111111111")).thenReturn(Optional.empty());
        when(userRepository.findByEmailIgnoreCase("ana@test.com"))
                .thenReturn(Optional.of(user(1L, "222222222", "Ana 2", 0, "No frecuente")));

        BusinessException exception = assertThrows(BusinessException.class, () -> userService.saveUser(payload));

        assertEquals("Ya existe un usuario registrado con ese correo electronico.", exception.getMessage());
    }

    @Test
    void updateUser_givenDuplicateEmailFromOtherUser_throwsBusinessException() {
        UserEntity existing = user(9L, "111111111", "Ana", 0, "No frecuente");
        existing.setEmail("ana@test.com");
        UserEntity other = user(10L, "222222222", "Beto", 0, "No frecuente");
        other.setEmail("nuevo@test.com");
        UserEntity payload = new UserEntity();
        payload.setEmail("nuevo@test.com");

        when(userRepository.findById(9L)).thenReturn(Optional.of(existing));
        when(userRepository.findByEmailIgnoreCase("nuevo@test.com")).thenReturn(Optional.of(other));

        BusinessException exception = assertThrows(BusinessException.class, () -> userService.updateUser(9L, payload));

        assertEquals("Ya existe un usuario registrado con ese correo electronico.", exception.getMessage());
    }

    @Test
    void validateReservationGuestAge_givenInvalidInputs_throwsExpectedMessages() {
        BusinessException nullUser = assertThrows(BusinessException.class,
                () -> userService.validateReservationGuestAge(null, LocalDate.now(), false));
        BusinessException nullDate = assertThrows(BusinessException.class,
                () -> userService.validateReservationGuestAge(user(1L, "111111111", "Ana", 0, "No frecuente"), null, false));
        UserEntity withoutBirthday = user(1L, "111111111", "Ana", 0, "No frecuente");
        withoutBirthday.setDateBirthday(null);
        BusinessException missingBirthday = assertThrows(BusinessException.class,
                () -> userService.validateReservationGuestAge(withoutBirthday, LocalDate.now(), false));

        assertEquals("El huesped no existe.", nullUser.getMessage());
        assertEquals("La reserva debe tener una fecha de check-in valida.", nullDate.getMessage());
        assertEquals("Todos los huespedes deben registrar una fecha de nacimiento valida.", missingBirthday.getMessage());
    }

    @Test
    void validateReservationGuestAge_givenFutureTooOldOrUnderageGuest_throwsExpectedMessages() {
        LocalDate reference = LocalDate.now().plusDays(5);
        UserEntity futureBirthday = user(1L, "111111111", "Ana", 0, "No frecuente");
        futureBirthday.setDateBirthday(reference.plusDays(1));
        UserEntity tooOld = user(2L, "222222222", "Beto", 0, "No frecuente");
        tooOld.setDateBirthday(reference.minusYears(101));
        UserEntity underageHolder = user(3L, "333333333", "Carla", 0, "No frecuente");
        underageHolder.setDateBirthday(reference.minusYears(17));

        assertEquals("La fecha de nacimiento no puede ser posterior al check-in de la reserva.",
                assertThrows(BusinessException.class,
                        () -> userService.validateReservationGuestAge(futureBirthday, reference, false)).getMessage());
        assertEquals("No se permiten reservas con huespedes mayores de 100 anos.",
                assertThrows(BusinessException.class,
                        () -> userService.validateReservationGuestAge(tooOld, reference, false)).getMessage());
        assertEquals("El huesped principal debe ser mayor de edad para realizar la reserva.",
                assertThrows(BusinessException.class,
                        () -> userService.validateReservationGuestAge(underageHolder, reference, true)).getMessage());
    }

    @Test
    void validateReservationGuestAge_givenAdultGuest_doesNotThrow() {
        UserEntity adult = user(1L, "111111111", "Ana", 0, "No frecuente");
        adult.setDateBirthday(LocalDate.now().minusYears(30));

        userService.validateReservationGuestAge(adult, LocalDate.now().plusDays(10), true);

        assertTrue(true);
    }

    @Test
    void rutHelpers_coverInvalidAndNullBranches() {
        assertEquals("", userService.normalizeRut(null));
        assertFalse(userService.isValidRut("1"));
        assertFalse(userService.isValidRut("ABC"));
        assertTrue(userService.isValidRut("11.111.111-1"));
    }

    @Test
    void saveUser_givenBlankEmailAndCustomOptionalFields_normalizesValues() {
        UserEntity payload = user(null, "111111111", " Ana ", -2, "");
        payload.setEmail("   ");
        payload.setPhoneNumber(" +56912345678 ");
        payload.setRole(" admin ");
        payload.setFailedLoginAttempts(-1);
        payload.setActive(null);
        payload.setDocumentId(" DOC123 ");
        payload.setNationality(" Argentina ");

        when(userRepository.findByRut("111111111")).thenReturn(Optional.empty());
        when(userRepository.save(any(UserEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserEntity result = userService.saveUser(payload);

        assertNull(result.getEmail());
        assertEquals("ADMIN", result.getRole());
        assertEquals(0, result.getFailedLoginAttempts());
        assertTrue(result.getActive());
        assertEquals("DOC123", result.getDocumentId());
        assertEquals("Argentina", result.getNationality());
    }

    private UserEntity user(Long id, String rut, String name, Integer visits, String category) {
        UserEntity user = new UserEntity();
        user.setId(id);
        user.setRut(rut);
        user.setName(name);
        user.setNumberVisits(visits);
        user.setCategory_frecuency(category);
        user.setDateBirthday(LocalDate.of(1990, 1, 15));
        return user;
    }
}
