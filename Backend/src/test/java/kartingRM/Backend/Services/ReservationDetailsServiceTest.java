package kartingRM.Backend.Services;

import kartingRM.Backend.Entities.ReservationDetailsEntity;
import kartingRM.Backend.Repositories.ReservationDetailsRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReservationDetailsServiceTest {

    @Mock
    private ReservationDetailsRepository reservationDetailsRepository;

    @InjectMocks
    private ReservationDetailsService reservationDetailsService;

    @Test
    void getAllReservationDetails_returnsRepositoryValues() {
        when(reservationDetailsRepository.findAll()).thenReturn(List.of(detail(1L, "Maria", 1000.0, 15000.0)));

        List<ReservationDetailsEntity> result = reservationDetailsService.getAllReservationDetails();

        assertEquals(1, result.size());
        assertEquals("Maria", result.get(0).getGuestName());
    }

    @Test
    void getReservationDetailById_givenExistingId_returnsDetail() {
        when(reservationDetailsRepository.findById(1L)).thenReturn(Optional.of(detail(1L, "Pedro", 500.0, 9000.0)));

        ReservationDetailsEntity result = reservationDetailsService.getReservationDetailById(1L);

        assertEquals("Pedro", result.getGuestName());
    }

    @Test
    void getReservationDetailById_givenUnknownId_throwsException() {
        when(reservationDetailsRepository.findById(99L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> reservationDetailsService.getReservationDetailById(99L));

        assertEquals("Detalle de reserva no encontrado con ID: 99", exception.getMessage());
    }

    @Test
    void saveReservationDetail_persistsDetail() {
        ReservationDetailsEntity payload = detail(null, "Sofia", 0.0, 23000.0);
        ReservationDetailsEntity saved = detail(2L, "Sofia", 0.0, 23000.0);
        when(reservationDetailsRepository.save(payload)).thenReturn(saved);

        ReservationDetailsEntity result = reservationDetailsService.saveReservationDetail(payload);

        assertEquals(2L, result.getId());
    }

    @Test
    void updateReservationDetail_updatesMutableFields() {
        ReservationDetailsEntity existing = detail(3L, "Diego", 0.0, 25000.0);
        ReservationDetailsEntity payload = detail(null, "Diego Soto", 1500.0, 23500.0);
        when(reservationDetailsRepository.findById(3L)).thenReturn(Optional.of(existing));
        when(reservationDetailsRepository.save(any(ReservationDetailsEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ReservationDetailsEntity result = reservationDetailsService.updateReservationDetail(3L, payload);

        assertEquals("Diego Soto", result.getGuestName());
        assertEquals(1500.0, result.getDiscount());
        assertEquals(23500.0, result.getFinalAmount());
    }

    @Test
    void deleteReservationDetail_deletesById() {
        doNothing().when(reservationDetailsRepository).deleteById(4L);

        reservationDetailsService.deleteReservationDetail(4L);

        verify(reservationDetailsRepository).deleteById(4L);
    }

    private ReservationDetailsEntity detail(Long id, String guestName, Double discount, Double finalAmount) {
        ReservationDetailsEntity detail = new ReservationDetailsEntity();
        detail.setId(id);
        detail.setGuestName(guestName);
        detail.setDiscount(discount);
        detail.setFinalAmount(finalAmount);
        detail.setUserId(1L);
        return detail;
    }
}
