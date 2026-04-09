package kartingRM.Backend.Controllers;

import kartingRM.Backend.Entities.UserEntity;
import kartingRM.Backend.Services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
@CrossOrigin("*")
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping
    public ResponseEntity<List<UserEntity>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserEntity> getUserById(@PathVariable("id") long id) {
        return ResponseEntity.ok(userService.findUserById(id));
    }

    @PostMapping
    public ResponseEntity<UserEntity> addUser(@RequestBody UserEntity user) {
        UserEntity existing = userService.getUserByRut(user.getRut());
        if (existing != null) {
            return ResponseEntity.ok(existing);
        }
        return ResponseEntity.ok(userService.saveUser(user));
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserEntity> updateUser(@PathVariable("id") Long id, @RequestBody UserEntity user) {
        return ResponseEntity.ok(userService.updateUser(id, user));
    }

    @PutMapping("/{id}/update-category")
    public ResponseEntity<UserEntity> updateCategoryFrequency(@PathVariable("id") Long userId) {
        return ResponseEntity.ok(userService.updateCategoryFrequency(userId));
    }

    @PutMapping("/{id}/update-visits")
    public ResponseEntity<UserEntity> updateNumberVisits(
            @PathVariable("id") Long userId,
            @RequestParam("visits") int newVisits
    ) {
        return ResponseEntity.ok(userService.updateNumberVisits(userId, newVisits));
    }

    @PutMapping("/{id}/increment-visits")
    public ResponseEntity<UserEntity> incrementVisitsAndUpdateCategory(@PathVariable("id") Long userId) {
        return ResponseEntity.ok(userService.incrementVisitsAndUpdateCategory(userId));
    }

    @GetMapping("/findByRut/{rut:.+}")
    public ResponseEntity<UserEntity> getUserByRut(@PathVariable String rut) {
        UserEntity user = userService.getUserByRut(rut);
        if (user == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(user);
    }
}
