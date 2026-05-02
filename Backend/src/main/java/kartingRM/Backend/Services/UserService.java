package kartingRM.Backend.Services;

import kartingRM.Backend.Entities.UserEntity;
import kartingRM.Backend.Exceptions.BusinessException;
import kartingRM.Backend.Exceptions.ResourceNotFoundException;
import kartingRM.Backend.Repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.Period;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

@Service
public class UserService {

    private static final int MIN_BOOKING_AGE = 18;
    private static final int MAX_REASONABLE_AGE = 100;
    private static final Pattern NAME_PATTERN = Pattern.compile("^[\\p{L}][\\p{L} .'-]{1,119}$");
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,}$", Pattern.CASE_INSENSITIVE);
    private static final Pattern PHONE_PATTERN = Pattern.compile("^\\+?[0-9 ]{8,15}$");

    @Autowired
    private UserRepository userRepository;

    private String calcularCategoriaPorVisitas(Integer visits) {
        int cantidad = (visits == null || visits < 0) ? 0 : visits;

        if (cantidad >= 7) {
            return "Muy frecuente";
        } else if (cantidad >= 5) {
            return "Frecuente";
        } else if (cantidad >= 2) {
            return "Regular";
        } else {
            return "No frecuente";
        }
    }

    private boolean normalizarUsuario(UserEntity user) {
        boolean modificado = false;

        if (user.getNumberVisits() == null || user.getNumberVisits() < 0) {
            user.setNumberVisits(0);
            modificado = true;
        }

        if (user.getCategory_frecuency() == null || user.getCategory_frecuency().isBlank()) {
            user.setCategory_frecuency(calcularCategoriaPorVisitas(user.getNumberVisits()));
            modificado = true;
        }

        return modificado;
    }

    private UserEntity obtenerUsuarioNormalizado(Long userId) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con ID: " + userId));

        if (normalizarUsuario(user)) {
            user = userRepository.save(user);
        }

        return user;
    }

    public UserEntity updateCategoryFrequency(Long userId) {
        UserEntity user = obtenerUsuarioNormalizado(userId);
        user.setCategory_frecuency(calcularCategoriaPorVisitas(user.getNumberVisits()));
        return userRepository.save(user);
    }

    public UserEntity updateNumberVisits(Long userId, int newVisits) {
        UserEntity user = obtenerUsuarioNormalizado(userId);

        int visitasSeguras = Math.max(newVisits, 0);
        user.setNumberVisits(visitasSeguras);
        user.setCategory_frecuency(calcularCategoriaPorVisitas(visitasSeguras));

        return userRepository.save(user);
    }

    public UserEntity incrementVisitsAndUpdateCategory(Long userId) {
        UserEntity user = obtenerUsuarioNormalizado(userId);

        int visitasActuales = user.getNumberVisits() == null ? 0 : user.getNumberVisits();
        int nuevasVisitas = visitasActuales + 1;

        user.setNumberVisits(nuevasVisitas);
        user.setCategory_frecuency(calcularCategoriaPorVisitas(nuevasVisitas));

        return userRepository.save(user);
    }

    public List<UserEntity> getAllUsers() {
        return userRepository.findAll();
    }

    public Optional<UserEntity> findUserByRut(String rut) {
        String normalizedRut = normalizeRut(rut);
        if (normalizedRut.isBlank()) {
            return Optional.empty();
        }

        Optional<UserEntity> optionalUser = userRepository.findByRut(normalizedRut);

        if (optionalUser.isPresent()) {
            UserEntity user = optionalUser.get();
            if (normalizarUsuario(user)) {
                user = userRepository.save(user);
            }
            return Optional.of(user);
        }

        return Optional.empty();
    }

    public UserEntity incrementVisits(String rut) {
        String normalizedRut = normalizeRut(rut);
        UserEntity user = userRepository.findByRut(normalizedRut)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con RUT: " + normalizedRut));

        if (normalizarUsuario(user)) {
            user = userRepository.save(user);
        }

        int visitasActuales = user.getNumberVisits() == null ? 0 : user.getNumberVisits();
        user.setNumberVisits(visitasActuales + 1);
        user.setCategory_frecuency(calcularCategoriaPorVisitas(user.getNumberVisits()));

        return userRepository.save(user);
    }

    public UserEntity saveUser(UserEntity user) {
        user.setRut(normalizeRut(user.getRut()));
        validateUser(user, true);
        if (userRepository.findByRut(user.getRut()).isPresent()) {
            throw new BusinessException("Ya existe un usuario registrado con ese RUT.");
        }
        if (user.getEmail() != null && !user.getEmail().isBlank()) {
            Optional<UserEntity> userWithEmail = userRepository.findByEmailIgnoreCase(user.getEmail().trim());
            if (userWithEmail != null && userWithEmail.isPresent()) {
                throw new BusinessException("Ya existe un usuario registrado con ese correo electronico.");
            }
        }
        normalizeUserForPersistence(user);
        return userRepository.save(user);
    }

    public UserEntity getUserByRut(String rut) {
        String normalizedRut = normalizeRut(rut);
        if (normalizedRut.isBlank()) {
            return null;
        }

        Optional<UserEntity> optionalUser = userRepository.findByRut(normalizedRut);

        if (optionalUser.isPresent()) {
            UserEntity user = optionalUser.get();
            if (normalizarUsuario(user)) {
                user = userRepository.save(user);
            }
            return user;
        }

        return null;
    }

    public double obtenerDescuentoPorCategoria(Long userId) {
        UserEntity user = obtenerUsuarioNormalizado(userId);
        String categoria = user.getCategory_frecuency();

        return switch (categoria) {
            case "Muy frecuente" -> 0.20;
            case "Frecuente" -> 0.10;
            case "Regular" -> 0.05;
            default -> 0.0;
        };
    }

    public String obtenerCategoriaCliente(Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException("El ID del usuario no puede ser nulo.");
        }

        UserEntity user = obtenerUsuarioNormalizado(userId);
        return calcularCategoriaPorVisitas(user.getNumberVisits());
    }

    public UserEntity findUserById(Long id) {
        return obtenerUsuarioNormalizado(id);
    }

    public UserEntity updateUser(Long id, UserEntity updatedUser) {
        UserEntity existing = findUserById(id);

        if (updatedUser.getName() != null) {
            existing.setName(updatedUser.getName().trim());
        }
        if (updatedUser.getEmail() != null) {
            existing.setEmail(updatedUser.getEmail().trim());
        }
        if (updatedUser.getPhoneNumber() != null) {
            existing.setPhoneNumber(updatedUser.getPhoneNumber().trim());
        }
        if (updatedUser.getDateBirthday() != null) {
            existing.setDateBirthday(updatedUser.getDateBirthday());
        }
        if (updatedUser.getDocumentId() != null) {
            existing.setDocumentId(updatedUser.getDocumentId().trim());
        }
        if (updatedUser.getNationality() != null) {
            existing.setNationality(updatedUser.getNationality().trim());
        }
        if (updatedUser.getActive() != null) {
            existing.setActive(updatedUser.getActive());
        }

        validateUser(existing, false);
        if (existing.getEmail() != null && !existing.getEmail().isBlank()) {
            Optional<UserEntity> userWithEmail = userRepository.findByEmailIgnoreCase(existing.getEmail().trim());
            if (userWithEmail != null && userWithEmail.isPresent() && !userWithEmail.get().getId().equals(existing.getId())) {
                throw new BusinessException("Ya existe un usuario registrado con ese correo electronico.");
            }
        }
        normalizeUserForPersistence(existing);
        return userRepository.save(existing);
    }

    public void validateReservationGuestAge(UserEntity user, LocalDate referenceDate, boolean requireAdultHolder) {
        if (user == null) {
            throw new BusinessException("El huesped no existe.");
        }

        if (referenceDate == null) {
            throw new BusinessException("La reserva debe tener una fecha de check-in valida.");
        }

        if (user.getDateBirthday() == null) {
            throw new BusinessException("Todos los huespedes deben registrar una fecha de nacimiento valida.");
        }

        if (user.getDateBirthday().isAfter(referenceDate)) {
            throw new BusinessException("La fecha de nacimiento no puede ser posterior al check-in de la reserva.");
        }

        int ageAtCheckIn = Period.between(user.getDateBirthday(), referenceDate).getYears();
        if (ageAtCheckIn > MAX_REASONABLE_AGE) {
            throw new BusinessException("No se permiten reservas con huespedes mayores de 100 anos.");
        }

        if (requireAdultHolder && ageAtCheckIn < MIN_BOOKING_AGE) {
            throw new BusinessException("El huesped principal debe ser mayor de edad para realizar la reserva.");
        }
    }

    public String normalizeRut(String rut) {
        if (rut == null) {
            return "";
        }

        return rut.replace(".", "")
                .replace("-", "")
                .trim()
                .toUpperCase();
    }

    public boolean isValidRut(String rut) {
        String normalizedRut = normalizeRut(rut);
        if (normalizedRut.length() < 2) {
            return false;
        }

        String numberPart = normalizedRut.substring(0, normalizedRut.length() - 1);
        char providedVerifier = normalizedRut.charAt(normalizedRut.length() - 1);

        if (!numberPart.chars().allMatch(Character::isDigit)) {
            return false;
        }

        int sum = 0;
        int multiplier = 2;
        for (int index = numberPart.length() - 1; index >= 0; index--) {
            sum += Character.getNumericValue(numberPart.charAt(index)) * multiplier;
            multiplier = multiplier == 7 ? 2 : multiplier + 1;
        }

        int remainder = 11 - (sum % 11);
        char expectedVerifier = switch (remainder) {
            case 11 -> '0';
            case 10 -> 'K';
            default -> Character.forDigit(remainder, 10);
        };

        return expectedVerifier == providedVerifier;
    }

    private void validateUser(UserEntity user, boolean validateRut) {
        if (user == null) {
            throw new BusinessException("Debe enviar un usuario valido.");
        }

        if (validateRut && (user.getRut() == null || user.getRut().isBlank())) {
            throw new BusinessException("El rut es obligatorio.");
        }

        if (validateRut && !isValidRut(user.getRut())) {
            throw new BusinessException("El RUT ingresado no es valido.");
        }

        if (user.getName() == null || user.getName().isBlank()) {
            throw new BusinessException("El nombre del usuario es obligatorio.");
        }

        String normalizedName = user.getName().trim();
        if (normalizedName.length() < 3 || normalizedName.length() > 120 || !NAME_PATTERN.matcher(normalizedName).matches()) {
            throw new BusinessException("El nombre del usuario debe ser realista y contener entre 3 y 120 caracteres.");
        }

        if (user.getDateBirthday() == null) {
            throw new BusinessException("La fecha de nacimiento es obligatoria.");
        }

        if (user.getDateBirthday().isAfter(LocalDate.now())) {
            throw new BusinessException("La fecha de nacimiento no puede estar en el futuro.");
        }

        if (Period.between(user.getDateBirthday(), LocalDate.now()).getYears() > MAX_REASONABLE_AGE) {
            throw new BusinessException("No se permiten usuarios con mas de 100 anos.");
        }

        if (user.getEmail() != null && !user.getEmail().isBlank() && !EMAIL_PATTERN.matcher(user.getEmail().trim()).matches()) {
            throw new BusinessException("El correo del usuario no tiene un formato valido.");
        }

        if (user.getPhoneNumber() != null && !user.getPhoneNumber().isBlank()
                && !PHONE_PATTERN.matcher(user.getPhoneNumber().trim()).matches()) {
            throw new BusinessException("El telefono del usuario no tiene un formato valido.");
        }
    }

    private void normalizeUserForPersistence(UserEntity user) {
        if (user.getNumberVisits() == null || user.getNumberVisits() < 0) {
            user.setNumberVisits(0);
        }

        if (user.getActive() == null) {
            user.setActive(Boolean.TRUE);
        }

        if (user.getFailedLoginAttempts() == null || user.getFailedLoginAttempts() < 0) {
            user.setFailedLoginAttempts(0);
        }

        if (user.getRole() == null || user.getRole().isBlank()) {
            user.setRole("CLIENT");
        } else {
            user.setRole(user.getRole().trim().toUpperCase());
        }

        if (user.getCategory_frecuency() == null || user.getCategory_frecuency().isBlank()) {
            user.setCategory_frecuency(calcularCategoriaPorVisitas(user.getNumberVisits()));
        }

        user.setName(user.getName().trim());

        if (user.getEmail() != null) {
            String email = user.getEmail().trim();
            user.setEmail(email.isBlank() ? null : email);
        }

        if (user.getPhoneNumber() != null) {
            user.setPhoneNumber(user.getPhoneNumber().trim());
        }

        if (user.getDocumentId() == null || user.getDocumentId().isBlank()) {
            user.setDocumentId(user.getRut());
        } else {
            user.setDocumentId(user.getDocumentId().trim());
        }

        if (user.getNationality() == null || user.getNationality().isBlank()) {
            user.setNationality("Chile");
        } else {
            user.setNationality(user.getNationality().trim());
        }
    }
}
