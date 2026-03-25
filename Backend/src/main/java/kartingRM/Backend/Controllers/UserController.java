package kartingRM.Backend.Controllers;

import kartingRM.Backend.Entities.UserEntity;
import kartingRM.Backend.Services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@CrossOrigin("*")
public class UserController {
    @Autowired
    private UserService userService;

    @GetMapping("/")
    public List<UserEntity> getAllUsers() {
        return userService.getAllUsers();
    }
    @GetMapping("/{id}")
    public UserEntity getUserById(@PathVariable("id") long id) {
        return userService.findUserById(id);
    }

    @PostMapping("/")
    public ResponseEntity<UserEntity> addUser(@RequestBody UserEntity user) {
        if (user.getRut() == null || user.getRut().isEmpty()) {
            return ResponseEntity.badRequest().body(null);
        }
        UserEntity savedUser = userService.saveUser(user);
        return ResponseEntity.ok(savedUser);
    }

    @PutMapping("/{id}/update-category")
    public UserEntity updateCategoryFrequency(@PathVariable("id") Long userId) {
        return userService.updateCategoryFrequency(userId);
    }

    @PutMapping("/{id}/update-visits")
    public UserEntity updateNumberVisits(@PathVariable("id") Long userId, @RequestParam("visits") int newVisits) {
        return userService.updateNumberVisits(userId, newVisits);
    }

    @PutMapping("/{id}/increment-visits")
    public ResponseEntity<?> incrementVisitsAndUpdateCategory(@PathVariable("id") Long userId) {
        try {
            UserEntity updatedUser = userService.incrementVisitsAndUpdateCategory(userId);
            return ResponseEntity.ok(updatedUser);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @GetMapping("/findByRut/{rut}")
    public ResponseEntity<UserEntity> getUserByRut(@PathVariable String rut) {
        UserEntity user = userService.getUserByRut(rut);
        if (user != null) {
            return ResponseEntity.ok(user);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }
}
