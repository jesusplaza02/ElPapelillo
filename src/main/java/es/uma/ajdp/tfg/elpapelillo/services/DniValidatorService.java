package es.uma.ajdp.tfg.elpapelillo.services;

import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Componente para la validación del Documento Nacional de Identidad (DNI) español.
 * Implementa el algoritmo oficial de verificación de la letra de control.
 */
@Component
public class DniValidatorService {

    private static final String LETRAS_DNI = "TRWAGMYFPDXBNJZSQVHLCKE";

    private static final Map<String, String> NIE_PREFIJO = new HashMap<>();

    static {
        NIE_PREFIJO.put("X", "0");
        NIE_PREFIJO.put("Y", "1");
        NIE_PREFIJO.put("Z", "2");
    }

    /**
     * Valida si un DNI o NIE español tiene el formato y letra de control correctos.
     *
     * @param dni cadena con el DNI o NIE a validar
     * @return true si el DNI/NIE es válido, false en caso contrario
     */
    public boolean validar(String dni) {
        if (dni == null || dni.isBlank()) {
            return false;
        }
        String dniLimpio = dni.trim().toUpperCase();

        if (dniLimpio.matches("[0-9]{8}[A-Z]")) {
            return validarDni(dniLimpio);
        } else if (dniLimpio.matches("[XYZ][0-9]{7}[A-Z]")) {
            return validarNie(dniLimpio);
        }
        return false;
    }

    private boolean validarDni(String dni) {
        int numero = Integer.parseInt(dni.substring(0, 8));
        char letraEsperada = LETRAS_DNI.charAt(numero % 23);
        return dni.charAt(8) == letraEsperada;
    }

    private boolean validarNie(String nie) {
        String prefijo = String.valueOf(nie.charAt(0));
        String nieNumerico = NIE_PREFIJO.get(prefijo) + nie.substring(1, 8);
        int numero = Integer.parseInt(nieNumerico);
        char letraEsperada = LETRAS_DNI.charAt(numero % 23);
        return nie.charAt(8) == letraEsperada;
    }
}
