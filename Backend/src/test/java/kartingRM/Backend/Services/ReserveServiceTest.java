package kartingRM.Backend.Services;

import kartingRM.Backend.Entities.ReserveDetailsEntity;
import kartingRM.Backend.Entities.ReserveEntity;
import kartingRM.Backend.Entities.UserEntity;
import kartingRM.Backend.Repositories.ReserveRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.mail.javamail.JavaMailSender;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ReserveServiceTest {

    @Mock
    private ReserveRepository reserveRepository;

    @Mock
    private UserService userService;
    @InjectMocks
    private ReserveService reserveService;

    @Mock
    private JavaMailSender mailSender;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        reserveService = Mockito.spy(reserveService);
    }

    @Test
    void testGetReporteIngresosPorVueltasOTiempo() {
        
        List<ReserveEntity> reservas = new ArrayList<>();
        ReserveEntity reserva1 = new ReserveEntity();
        reserva1.setDetalles(new ArrayList<>(List.of(new ReserveDetailsEntity(), new ReserveDetailsEntity())));
        reserva1.setFecha_uso(LocalDate.of(2024, 1, 15));
        reserva1.setVueltas_o_tiempo("10 vueltas");
        reserva1.setMontoFinal(4000.0);
        reservas.add(reserva1);

        when(reserveRepository.findAll()).thenReturn(reservas);

        
        var reporte = reserveService.getReporteIngresosPorVueltasOTiempo(
                LocalDate.of(2024, 1, 1),
                LocalDate.of(2024, 1, 31)
        );

        
        assertEquals(4000.0, reporte.get("10 vueltas o máx 10 min").get("Enero"));
        assertEquals(4000.0, reporte.get("10 vueltas o máx 10 min").get("TOTAL"));
    }
    @Test
    void testGetReporteIngresosPorCantidadDePersonas() {
        ReserveEntity reserva = new ReserveEntity();
        reserva.setFecha_uso(LocalDate.of(2024, 1, 15));
        reserva.setDetalles(new ArrayList<>(List.of(new ReserveDetailsEntity())));
        reserva.getDetalles().get(0).setMontoFinal(3000.0);

        when(reserveRepository.findAll()).thenReturn(List.of(reserva));

        var reporte = reserveService.getReporteIngresosPorCantidadDePersonas(
                LocalDate.of(2024, 1, 1),
                LocalDate.of(2024, 1, 31)
        );

        assertEquals(3000.0, reporte.get("1-2 personas").get("Enero"));
    }
    
    @Test
    void testEsDiaFeriado() {
        assertTrue(reserveService.esDiaFeriado(LocalDate.of(2025, 9, 18))); // Fiestas Patrias
        assertTrue(reserveService.esDiaFeriado(LocalDate.of(2025, 12, 25))); // Navidad
        assertFalse(reserveService.esDiaFeriado(LocalDate.of(2025, 4, 25))); // Día normal
    }

    @Test
    void testEsFinDeSemana() {
        assertTrue(reserveService.esFinDeSemana(LocalDate.of(2025, 4, 26))); // Sábado
        assertTrue(reserveService.esFinDeSemana(LocalDate.of(2025, 4, 27))); // Domingo
        assertFalse(reserveService.esFinDeSemana(LocalDate.of(2025, 4, 28))); // Lunes
    }

    @Test
    void testGetRangoPorCantidadDePersonas() {
        assertEquals("1-2 personas", reserveService.getRangoPorCantidadDePersonas(1));
        assertEquals("1-2 personas", reserveService.getRangoPorCantidadDePersonas(2));
        assertEquals("3-5 personas", reserveService.getRangoPorCantidadDePersonas(3));
        assertEquals("3-5 personas", reserveService.getRangoPorCantidadDePersonas(5));
        assertEquals("6-10 personas", reserveService.getRangoPorCantidadDePersonas(6));
        assertEquals("6-10 personas", reserveService.getRangoPorCantidadDePersonas(10));
        assertEquals("11-15 personas", reserveService.getRangoPorCantidadDePersonas(11));
        assertEquals("11-15 personas", reserveService.getRangoPorCantidadDePersonas(15));
        assertNull(reserveService.getRangoPorCantidadDePersonas(16)); // Caso fuera de rango
        assertNull(reserveService.getRangoPorCantidadDePersonas(0));  // Caso fuera de rango
    }

    @Test
    void testUpdateReserveNotFound() {
        Long id = 1L;
        ReserveEntity updatedReserva = new ReserveEntity();
        updatedReserva.setMontoFinal(6000.0);

        when(reserveRepository.findById(id)).thenReturn(Optional.empty());

        Exception exception = assertThrows(RuntimeException.class, () -> {
            reserveService.updateReserve(id, updatedReserva);
        });

        assertEquals("Reserva no encontrada con ID: " + id, exception.getMessage());
        verify(reserveRepository, times(1)).findById(id);
    }

    @Test
    void testGenerarComprobantePdf() {
        try {
            ReserveEntity reserva = new ReserveEntity();
            reserva.setCodigo_reserva("RES123");
            reserva.setFecha_uso(LocalDate.of(2025, 4, 25));
            reserva.setCantidad_personas(3);

            ReserveDetailsEntity detalle = new ReserveDetailsEntity();
            detalle.setMemberName("Juan");
            detalle.setMontoFinal(10000.0);
            detalle.setDiscount(0.10);

            reserva.setDetalles(List.of(detalle));

            byte[] pdfBytes = reserveService.generarComprobantePdf(reserva);

            assertNotNull(pdfBytes);
            assertTrue(pdfBytes.length > 0);
        } catch (IOException e) {
            fail("Se lanzó una excepción inesperada: " + e.getMessage());
        }
    }

    @Test
    void testEnviarComprobantePorCorreo() {
        try {
            String[] destinatarios = {"test@example.com"};
            byte[] pdfBytes = new byte[10];
            String reserveCode = "RES123";

            MimeMessage mimeMessage = Mockito.mock(MimeMessage.class);
            when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

            doNothing().when(mailSender).send(any(MimeMessage.class));

            reserveService.enviarComprobantePorCorreo(destinatarios, pdfBytes, reserveCode);

            verify(mailSender, times(1)).send(any(MimeMessage.class));
        } catch (MessagingException e) {
            fail("Se lanzó una excepción inesperada: " + e.getMessage());
        }
    }

    @Test
    void testSaveReserveConDescuentos() {
        ReserveEntity reserva = new ReserveEntity();
        reserva.setCodigo_reserva("RES123");
        reserva.setFecha_uso(LocalDate.of(2025, 4, 25));
        reserva.setCantidad_personas(5);
        reserva.setVueltas_o_tiempo("10 vueltas");

        ReserveDetailsEntity detalle1 = new ReserveDetailsEntity();
        detalle1.setMemberName("Juan");
        detalle1.setDateBirthday(LocalDate.of(2025, 4, 25));
        detalle1.setMontoFinal(10000.0);
        detalle1.setUserId(1L);

        ReserveDetailsEntity detalle2 = new ReserveDetailsEntity();
        detalle2.setMemberName("Pedro");
        detalle2.setDateBirthday(LocalDate.of(1990, 1, 1));
        detalle2.setMontoFinal(15000.0);
        detalle2.setUserId(2L);

        reserva.setDetalles(List.of(detalle1, detalle2));

        UserEntity user1 = new UserEntity();
        user1.setEmail("juan@example.com");

        UserEntity user2 = new UserEntity();
        user2.setEmail("pedro@example.com");

        when(userService.obtenerDescuentoPorCategoria(anyLong())).thenReturn(0.10);
        when(userService.findUserById(1L)).thenReturn(user1);
        when(userService.findUserById(2L)).thenReturn(user2);
        when(reserveRepository.save(any(ReserveEntity.class))).thenReturn(reserva);

        // Mock del MimeMessage
        MimeMessage mimeMessage = Mockito.mock(MimeMessage.class);
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        doNothing().when(mailSender).send(any(MimeMessage.class));

        ReserveEntity result = reserveService.saveReserve(reserva);

        assertNotNull(result);
        assertEquals(2, result.getDetalles().size());
        verify(reserveRepository, times(1)).save(reserva);
        verify(mailSender, times(1)).send(any(MimeMessage.class));
    }
    @Test
    void testSaveReserveNormal() {
        ReserveEntity reserva = new ReserveEntity();
        reserva.setCodigo_reserva("RES456");
        reserva.setFecha_uso(LocalDate.of(2025, 5, 15));
        reserva.setCantidad_personas(3);
        reserva.setVueltas_o_tiempo("15 vueltas");

        ReserveDetailsEntity detalle = new ReserveDetailsEntity();
        detalle.setMemberName("Carlos");
        detalle.setDateBirthday(LocalDate.of(1990, 6, 10));
        detalle.setMontoFinal(20000.0);
        detalle.setUserId(1L); // Asegúrate de configurar el userId

        reserva.setDetalles(List.of(detalle));

        // Mock del UserEntity
        UserEntity user = new UserEntity();
        user.setEmail("carlos@example.com");

        // Configurar los mocks
        when(userService.findUserById(1L)).thenReturn(user);
        when(reserveRepository.save(any(ReserveEntity.class))).thenReturn(reserva);

        // Mock del MimeMessage
        MimeMessage mimeMessage = Mockito.mock(MimeMessage.class);
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        doNothing().when(mailSender).send(any(MimeMessage.class));

        ReserveEntity result = reserveService.saveReserve(reserva);

        // Verificaciones
        assertNotNull(result);
        assertEquals("RES456", result.getCodigo_reserva());
        assertEquals(1, result.getDetalles().size());
        assertEquals("Carlos", result.getDetalles().get(0).getMemberName());
        verify(userService, times(1)).findUserById(1L);
        verify(reserveRepository, times(1)).save(reserva);
        verify(mailSender, times(1)).send(any(MimeMessage.class));
    }

    @Test
    void testGetReserveByIdNotFound() {
        Long id = 1L;

        when(reserveRepository.findById(id)).thenReturn(Optional.empty());

        Exception exception = assertThrows(RuntimeException.class, () -> {
            reserveService.getReserveById(id);
        });

        assertEquals("Reserva no encontrada con ID: " + id, exception.getMessage());
        verify(reserveRepository, times(1)).findById(id);
    }

    @Test
    void testUpdateReserve() {
        Long id = 1L;
        ReserveEntity existingReserva = new ReserveEntity();
        existingReserva.setId(id);
        existingReserva.setMontoFinal(4000.0);

        ReserveEntity updatedReserva = new ReserveEntity();
        updatedReserva.setMontoFinal(6000.0);

        when(reserveRepository.findById(id)).thenReturn(java.util.Optional.of(existingReserva));
        when(reserveRepository.save(existingReserva)).thenReturn(existingReserva);

        ReserveEntity result = reserveService.updateReserve(id, updatedReserva);

        assertEquals(6000.0, result.getMontoFinal());
    }
    @Test
    void testDeleteReserve() {
        Long id = 1L;

        reserveService.deleteReserve(id);

        Mockito.verify(reserveRepository).deleteById(id);
    }
    @Test
    void testGetReserveById() {
        Long id = 1L;
        ReserveEntity reserva = new ReserveEntity();
        reserva.setId(id);

        when(reserveRepository.findById(id)).thenReturn(java.util.Optional.of(reserva));

        ReserveEntity result = reserveService.getReserveById(id);

        assertEquals(id, result.getId());
    }
    @Test
    void testGetAllReserves() {
        List<ReserveEntity> reservas = new ArrayList<>();
        reservas.add(new ReserveEntity());
        reservas.add(new ReserveEntity());

        when(reserveRepository.findAll()).thenReturn(reservas);

        List<ReserveEntity> result = reserveService.getAllReserves();

        assertEquals(2, result.size());
    }
    @Test
    void testCalcularTarifaBase() {
        double tarifa10Vueltas = reserveService.calcularTarifaBase("10 vueltas");
        double tarifa15Vueltas = reserveService.calcularTarifaBase("15 vueltas");
        double tarifa20Vueltas = reserveService.calcularTarifaBase("20 vueltas");

        assertEquals(15000, tarifa10Vueltas);
        assertEquals(20000, tarifa15Vueltas);
        assertEquals(25000, tarifa20Vueltas);
    }
    @Test
    void testCalcularDescuentoGrupo() {
        double descuento1Persona = reserveService.calcularDescuentoGrupo(1);
        double descuento5Personas = reserveService.calcularDescuentoGrupo(5);
        double descuento7Personas = reserveService.calcularDescuentoGrupo(7);
        double descuento12Personas = reserveService.calcularDescuentoGrupo(12);

        assertEquals(0.0, descuento1Persona);
        assertEquals(0.10, descuento5Personas);
        assertEquals(0.20, descuento7Personas);
        assertEquals(0.30, descuento12Personas);
    }
}