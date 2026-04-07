package es.uma.ajdp.tfg.elpapelillo;

import es.uma.ajdp.tfg.elpapelillo.services.DniValidatorService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitarios para el servicio de validación de DNI/NIE.
 */
class DniValidatorServiceTest {

    private final DniValidatorService validator = new DniValidatorService();

    @Test
    void dnisValidosDebenRetornarTrue() {
        assertTrue(validator.validar("12345678Z"));
        assertTrue(validator.validar("00000000T"));
    }

    @Test
    void nieValidoDebeRetornarTrue() {
        assertTrue(validator.validar("X1234567L"));
    }

    @Test
    void dniConLetraIncorrectaDebeRetornarFalse() {
        assertFalse(validator.validar("12345678A"));
    }

    @Test
    void dniNuloDebeRetornarFalse() {
        assertFalse(validator.validar(null));
    }

    @Test
    void dniVacioDebeRetornarFalse() {
        assertFalse(validator.validar(""));
        assertFalse(validator.validar("   "));
    }

    @ParameterizedTest
    @ValueSource(strings = {"1234567", "ABCDEFGHI", "1234567AB", "123456789"})
    void dniConFormatoInvalidoDebeRetornarFalse(String dni) {
        assertFalse(validator.validar(dni));
    }
}
