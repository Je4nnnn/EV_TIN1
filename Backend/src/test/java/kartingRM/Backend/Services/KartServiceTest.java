package kartingRM.Backend.Services;

import kartingRM.Backend.Entities.KartEntity;
import kartingRM.Backend.Repositories.KartRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class KartServiceTest {

    @Mock
    private KartRepository kartRepository;

    @InjectMocks
    private KartService kartService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetAllKarts() {
        List<KartEntity> karts = new ArrayList<>();
        karts.add(new KartEntity());
        karts.add(new KartEntity());

        when(kartRepository.findAll()).thenReturn(karts);

        List<KartEntity> result = kartService.getAllKarts();

        assertEquals(2, result.size());
        verify(kartRepository, times(1)).findAll();
    }

    @Test
    void testGetKartById() {
        long id = 1L;
        KartEntity kart = new KartEntity();
        kart.setId(id);

        when(kartRepository.findById(id)).thenReturn(Optional.of(kart));

        KartEntity result = kartService.getKartById(id);

        assertNotNull(result);
        assertEquals(id, result.getId());
        verify(kartRepository, times(1)).findById(id);
    }

    @Test
    void testGetKartByIdNotFound() {
        long id = 1L;

        when(kartRepository.findById(id)).thenReturn(Optional.empty());

        Exception exception = assertThrows(RuntimeException.class, () -> {
            kartService.getKartById(id);
        });

        assertTrue(exception instanceof RuntimeException);
        verify(kartRepository, times(1)).findById(id);
    }

    @Test
    void testSaveKart() {
        KartEntity kart = new KartEntity();
        kart.setCode("Kart 1");

        when(kartRepository.save(kart)).thenReturn(kart);

        KartEntity result = kartService.saveKart(kart);

        assertNotNull(result);
        assertEquals("Kart 1", result.getCode());
        verify(kartRepository, times(1)).save(kart);
    }
}