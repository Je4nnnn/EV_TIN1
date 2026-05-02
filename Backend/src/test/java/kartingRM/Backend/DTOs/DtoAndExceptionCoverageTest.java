package kartingRM.Backend.DTOs;

import kartingRM.Backend.Entities.ReservationDetailsEntity;
import kartingRM.Backend.Entities.ReservationEntity;
import kartingRM.Backend.Exceptions.BusinessException;
import kartingRM.Backend.Exceptions.GlobalExceptionHandler;
import kartingRM.Backend.Exceptions.ResourceNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;

import java.lang.reflect.Method;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DtoAndExceptionCoverageTest {

    @Test
    void dtoRecordsExposeAssignedValues() {
        ApiMessageResponse messageResponse = new ApiMessageResponse("ok");
        ApiErrorResponse errorResponse = new ApiErrorResponse(LocalDateTime.of(2026, 4, 27, 12, 0), 400, "Bad Request", "detalle", "/api/test");
        AuthenticatedUserResponse authenticatedUserResponse = new AuthenticatedUserResponse(
                "subject",
                "hotelrm-admin",
                "Admin",
                "admin@hotelrm.test",
                List.of("ROLE_HOTELRM_ADMIN")
        );
        RoomAvailabilityResponse roomAvailabilityResponse = new RoomAvailabilityResponse(
                1L,
                "S001",
                "Simple",
                "AVAILABLE",
                "RESERVED",
                "RSV001",
                LocalDate.of(2026, 4, 27),
                LocalDate.of(2026, 4, 28)
        );

        assertEquals("ok", messageResponse.message());
        assertEquals(400, errorResponse.status());
        assertEquals("hotelrm-admin", authenticatedUserResponse.username());
        assertEquals("RESERVED", roomAvailabilityResponse.occupancyStatus());
    }

    @Test
    void globalExceptionHandler_returnsTypedResponses() {
        GlobalExceptionHandler handler = new GlobalExceptionHandler();
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/v1/demo");

        ResponseEntity<ApiErrorResponse> notFound = handler.handleNotFound(new ResourceNotFoundException("no existe"), request);
        ResponseEntity<ApiErrorResponse> badRequest = handler.handleBadRequest(new BusinessException("invalido"), request);
        ResponseEntity<ApiErrorResponse> unexpected = handler.handleUnexpected(new RuntimeException("boom"), request);

        assertEquals(404, notFound.getStatusCode().value());
        assertEquals("no existe", notFound.getBody().message());
        assertEquals(400, badRequest.getStatusCode().value());
        assertEquals("invalido", badRequest.getBody().message());
        assertEquals(500, unexpected.getStatusCode().value());
        assertEquals("/api/v1/demo", unexpected.getBody().path());
    }

    @Test
    void reservationEntities_generateDefaultsAndUsefulStrings() throws Exception {
        ReservationEntity reservation = new ReservationEntity();
        reservation.setCheckInDate(LocalDate.of(2026, 4, 27));
        reservation.setCheckOutDate(LocalDate.of(2026, 4, 28));
        reservation.setStayType("Noche");
        reservation.setRoomType("Simple");
        reservation.setFinalAmount(80000.0);
        reservation.setCancelled(null);

        Method prepareDefaults = ReservationEntity.class.getDeclaredMethod("prepareDefaults");
        prepareDefaults.setAccessible(true);
        prepareDefaults.invoke(reservation);

        assertNotNull(reservation.getReservationCode());
        assertEquals(false, reservation.getCancelled());
        assertTrue(reservation.toString().contains("Simple"));

        ReservationDetailsEntity detail = new ReservationDetailsEntity();
        detail.setId(10L);
        detail.setGuestName("Maria");
        detail.setFinalAmount(20000.0);
        detail.setDiscount(1000.0);

        assertTrue(detail.toString().contains("Maria"));
    }
}
