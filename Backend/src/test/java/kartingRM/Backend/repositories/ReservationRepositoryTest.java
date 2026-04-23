package kartingRM.Backend.repositories;

import kartingRM.Backend.Entities.ReservationDetailsEntity;
import kartingRM.Backend.Entities.ReservationEntity;
import kartingRM.Backend.Entities.UserEntity;
import kartingRM.Backend.Repositories.ReservationRepository;
import kartingRM.Backend.Repositories.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class ReservationRepositoryTest {

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    void findByClienteRutIgnoreCaseAndCancelledFalse_returnsOnlyActiveReservationsForRut() {
        UserEntity client = new UserEntity();
        client.setRut("11-1");
        client.setName("Cliente");
        client = userRepository.save(client);

        reservationRepository.save(buildReservation(client, "A001", false));
        reservationRepository.save(buildReservation(client, "A002", true));

        List<ReservationEntity> found = reservationRepository.findByClienteRutIgnoreCaseAndCancelledFalse("11-1");

        assertThat(found).hasSize(1);
        assertThat(found.get(0).getReservationCode()).isEqualTo("A001");
    }

    private ReservationEntity buildReservation(UserEntity client, String code, boolean cancelled) {
        ReservationEntity reservation = new ReservationEntity();
        reservation.setReservationCode(code);
        reservation.setCheckInDate(LocalDate.of(2026, 5, 10));
        reservation.setCheckOutDate(LocalDate.of(2026, 5, 11));
        reservation.setStayType("Noche");
        reservation.setCliente(client);
        reservation.setNumberOfGuests(1);
        reservation.setRoomType("Simple");
        reservation.setFinalAmount(50000.0);
        reservation.setRoomId(1L);
        reservation.setRoomNumber("S001");
        reservation.setCancelled(cancelled);

        ReservationDetailsEntity detail = new ReservationDetailsEntity();
        detail.setGuestName("Ana");
        detail.setDiscount(0.0);
        detail.setFinalAmount(50000.0);
        detail.setUserId(client.getId());
        detail.setReservation(reservation);
        reservation.setDetails(List.of(detail));

        return reservation;
    }
}
