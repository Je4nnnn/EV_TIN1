package kartingRM.Backend.Services;

import kartingRM.Backend.Entities.ReserveDetailsEntity;
import kartingRM.Backend.Repositories.ReserveDetailsRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ReserveDetailsServiceTest {

    @Mock
    private ReserveDetailsRepository reserveDetailsRepository;

    @InjectMocks
    private ReserveDetailsService reserveDetailsService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetAllReserveDetails() {
        List<ReserveDetailsEntity> detalles = new ArrayList<>();
        detalles.add(new ReserveDetailsEntity());
        detalles.add(new ReserveDetailsEntity());

        when(reserveDetailsRepository.findAll()).thenReturn(detalles);

        List<ReserveDetailsEntity> result = reserveDetailsService.getAllReserveDetails();

        assertEquals(2, result.size());
        verify(reserveDetailsRepository, times(1)).findAll();
    }

    @Test
    void testGetReserveDetailById() {
        Long id = 1L;
        ReserveDetailsEntity detalle = new ReserveDetailsEntity();
        detalle.setId(id);

        when(reserveDetailsRepository.findById(id)).thenReturn(Optional.of(detalle));

        ReserveDetailsEntity result = reserveDetailsService.getReserveDetailById(id);

        assertEquals(id, result.getId());
        verify(reserveDetailsRepository, times(1)).findById(id);
    }
    @Test
    void testSaveReserveDetail() {
        ReserveDetailsEntity detalle = new ReserveDetailsEntity();
        detalle.setMemberName("John Doe");

        when(reserveDetailsRepository.save(detalle)).thenReturn(detalle);

        ReserveDetailsEntity result = reserveDetailsService.saveReserveDetail(detalle);

        assertEquals("John Doe", result.getMemberName());
        verify(reserveDetailsRepository, times(1)).save(detalle);
    }
    @Test
    void testUpdateReserveDetail() {
        Long id = 1L;
        ReserveDetailsEntity existingDetail = new ReserveDetailsEntity();
        existingDetail.setId(id);
        existingDetail.setMemberName("John Doe");

        ReserveDetailsEntity updatedDetail = new ReserveDetailsEntity();
        updatedDetail.setMemberName("Jane Doe");
        updatedDetail.setDateBirthday(LocalDate.of(1990, 1, 1));
        updatedDetail.setDiscount(0.10);

        when(reserveDetailsRepository.findById(id)).thenReturn(Optional.of(existingDetail));
        when(reserveDetailsRepository.save(existingDetail)).thenReturn(existingDetail);

        ReserveDetailsEntity result = reserveDetailsService.updateReserveDetail(id, updatedDetail);

        assertEquals("Jane Doe", result.getMemberName());
        assertEquals(LocalDate.of(1990, 1, 1), result.getDateBirthday());
        assertEquals(0.10, result.getDiscount());
        verify(reserveDetailsRepository, times(1)).findById(id);
        verify(reserveDetailsRepository, times(1)).save(existingDetail);
    }
    @Test
    void testDeleteReserveDetail() {
        Long id = 1L;

        doNothing().when(reserveDetailsRepository).deleteById(id);

        reserveDetailsService.deleteReserveDetail(id);

        verify(reserveDetailsRepository, times(1)).deleteById(id);
    }
    @Test
    void testGetReserveDetailByIdNotFound() {
        Long id = 1L;

        when(reserveDetailsRepository.findById(id)).thenReturn(Optional.empty());

        Exception exception = assertThrows(RuntimeException.class, () -> {
            reserveDetailsService.getReserveDetailById(id);
        });

        assertEquals("Detalle de reserva no encontrado con ID: " + id, exception.getMessage());
        verify(reserveDetailsRepository, times(1)).findById(id);
    }
}