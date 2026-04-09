package kartingRM.Backend.Services;

import kartingRM.Backend.Entities.UserEntity;
import kartingRM.Backend.Exceptions.BusinessException;
import kartingRM.Backend.Exceptions.ResourceNotFoundException;
import kartingRM.Backend.Repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {

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
        if (rut == null || rut.isBlank()) {
            return Optional.empty();
        }

        Optional<UserEntity> optionalUser = userRepository.findByRut(rut);

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
        UserEntity user = userRepository.findByRut(rut)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con RUT: " + rut));

        if (normalizarUsuario(user)) {
            user = userRepository.save(user);
        }

        int visitasActuales = user.getNumberVisits() == null ? 0 : user.getNumberVisits();
        user.setNumberVisits(visitasActuales + 1);
        user.setCategory_frecuency(calcularCategoriaPorVisitas(user.getNumberVisits()));

        return userRepository.save(user);
    }

    public UserEntity saveUser(UserEntity user) {
        validateUser(user, true);
        user.setRut(user.getRut().trim().toUpperCase());
        normalizeUserForPersistence(user);
        return userRepository.save(user);
    }

    public UserEntity getUserByRut(String rut) {
        if (rut == null || rut.isBlank()) {
            return null;
        }

        Optional<UserEntity> optionalUser = userRepository.findByRut(rut);

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

        validateUser(existing, false);
        normalizeUserForPersistence(existing);
        return userRepository.save(existing);
    }

    private void validateUser(UserEntity user, boolean validateRut) {
        if (user == null) {
            throw new BusinessException("Debe enviar un usuario valido.");
        }

        if (validateRut && (user.getRut() == null || user.getRut().isBlank())) {
            throw new BusinessException("El rut es obligatorio.");
        }

        if (user.getName() == null || user.getName().isBlank()) {
            throw new BusinessException("El nombre del usuario es obligatorio.");
        }

        if (user.getEmail() != null && !user.getEmail().isBlank() && !user.getEmail().contains("@")) {
            throw new BusinessException("El correo del usuario no tiene un formato valido.");
        }
    }

    private void normalizeUserForPersistence(UserEntity user) {
        if (user.getNumberVisits() == null || user.getNumberVisits() < 0) {
            user.setNumberVisits(0);
        }

        if (user.getCategory_frecuency() == null || user.getCategory_frecuency().isBlank()) {
            user.setCategory_frecuency(calcularCategoriaPorVisitas(user.getNumberVisits()));
        }

        user.setName(user.getName().trim());

        if (user.getEmail() != null) {
            user.setEmail(user.getEmail().trim());
        }

        if (user.getPhoneNumber() != null) {
            user.setPhoneNumber(user.getPhoneNumber().trim());
        }
    }
}
