package kartingRM.Backend.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import kartingRM.Backend.Controllers.UserController;
import kartingRM.Backend.Entities.UserEntity;
import kartingRM.Backend.Exceptions.GlobalExceptionHandler;
import kartingRM.Backend.Services.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private UserService userService;

    @InjectMocks
    private UserController userController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(userController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void getUserByRut_givenExistingUser_thenReturnsOk() throws Exception {
        UserEntity user = buildUser(1L, "11-1", "Diego");
        when(userService.getUserByRut("11-1")).thenReturn(user);

        mockMvc.perform(get("/api/v1/users/findByRut/11-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.rut").value("11-1"))
                .andExpect(jsonPath("$.name").value("Diego"));
    }

    @Test
    void getUserByRut_givenUnknownUser_thenReturnsNotFound() throws Exception {
        when(userService.getUserByRut("99-9")).thenReturn(null);

        mockMvc.perform(get("/api/v1/users/findByRut/99-9"))
                .andExpect(status().isNotFound());
    }

    @Test
    void addUser_givenExistingRut_thenReturnsExistingUserWithoutSaving() throws Exception {
        UserEntity existingUser = buildUser(2L, "22-2", "Sofia");
        when(userService.getUserByRut("22-2")).thenReturn(existingUser);

        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildUser(null, "22-2", "Sofia"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(2))
                .andExpect(jsonPath("$.rut").value("22-2"));
    }

    @Test
    void addUser_givenNewRut_thenPersistsAndReturnsUser() throws Exception {
        UserEntity payload = buildUser(null, "33-3", "Juan");
        UserEntity saved = buildUser(3L, "33-3", "Juan");
        when(userService.getUserByRut("33-3")).thenReturn(null);
        when(userService.saveUser(any(UserEntity.class))).thenReturn(saved);

        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(3))
                .andExpect(jsonPath("$.rut").value("33-3"))
                .andExpect(jsonPath("$.name").value("Juan"));
    }

    @Test
    void getAllUsers_thenReturnsUsers() throws Exception {
        when(userService.getAllUsers()).thenReturn(List.of(buildUser(1L, "11-1", "Diego")));

        mockMvc.perform(get("/api/v1/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1));
    }

    @Test
    void getUserById_thenReturnsUser() throws Exception {
        when(userService.findUserById(4L)).thenReturn(buildUser(4L, "44-4", "Laura"));

        mockMvc.perform(get("/api/v1/users/4"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Laura"));
    }

    @Test
    void updateUser_thenReturnsUpdatedUser() throws Exception {
        UserEntity payload = buildUser(null, "55-5", "Paula");
        UserEntity updated = buildUser(5L, "55-5", "Paula");
        when(userService.updateUser(any(Long.class), any(UserEntity.class))).thenReturn(updated);

        mockMvc.perform(put("/api/v1/users/5")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(5));
    }

    @Test
    void updateCategoryFrequency_thenReturnsUser() throws Exception {
        when(userService.updateCategoryFrequency(6L)).thenReturn(buildUser(6L, "66-6", "Mario"));

        mockMvc.perform(put("/api/v1/users/6/update-category"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(6));
    }

    @Test
    void updateNumberVisits_thenReturnsUser() throws Exception {
        when(userService.updateNumberVisits(7L, 3)).thenReturn(buildUser(7L, "77-7", "Sofia"));

        mockMvc.perform(put("/api/v1/users/7/update-visits?visits=3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(7));
    }

    @Test
    void incrementVisitsAndUpdateCategory_thenReturnsUser() throws Exception {
        when(userService.incrementVisitsAndUpdateCategory(8L)).thenReturn(buildUser(8L, "88-8", "Camila"));

        mockMvc.perform(put("/api/v1/users/8/increment-visits"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(8));
    }

    private UserEntity buildUser(Long id, String rut, String name) {
        UserEntity user = new UserEntity();
        user.setId(id);
        user.setRut(rut);
        user.setName(name);
        user.setEmail(name.toLowerCase() + "@hotelrm.test");
        user.setPhoneNumber("+56911111111");
        return user;
    }
}
