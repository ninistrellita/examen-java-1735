package com.carmotors.vista;

import java.io.PrintStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Scanner;

public final class EntradaConsola {
    private EntradaConsola() {}

    public static String texto(Scanner entrada, PrintStream salida, String mensaje,
                               int limite, boolean opcional, boolean soloLetras) {
        while (true) {
            salida.print(mensaje);
            String valor = entrada.nextLine().strip();
            if (opcional && valor.isEmpty()) return valor;
            if (valor.isEmpty() || valor.length() > limite) {
                salida.println("Ingresa entre 1 y " + limite + " caracteres.");
            } else if (soloLetras && !valor.matches("[\\p{L}\\p{M}]+(?:[ '\\-][\\p{L}\\p{M}]+)*")) {
                salida.println("Usa letras y espacios, sin números.");
            } else {
                return valor;
            }
        }
    }

    public static int entero(Scanner entrada, PrintStream salida, String mensaje, int minimo, int maximo) {
        while (true) {
            salida.print(mensaje);
            String valor = entrada.nextLine().strip();
            try {
                if (!valor.matches("[0-9]+")) throw new NumberFormatException();
                int numero = Integer.parseInt(valor);
                if (numero >= minimo && numero <= maximo) return numero;
            } catch (NumberFormatException e) {
                // El mismo campo se vuelve a pedir.
            }
            salida.println("Ingresa un entero entre " + minimo + " y " + maximo + ".");
        }
    }

    public static BigDecimal decimal(Scanner entrada, PrintStream salida, String mensaje) {
        while (true) {
            salida.print(mensaje);
            String valor = entrada.nextLine().strip().replace(',', '.');
            if (valor.matches("[0-9]+(?:\\.[0-9]{1,2})?")) {
                BigDecimal numero = new BigDecimal(valor);
                if (numero.compareTo(new BigDecimal("99999999.99")) <= 0) return numero;
            }
            salida.println("Ingresa un valor de 0 a 99999999.99, con máximo dos decimales.");
        }
    }

    public static LocalDate fecha(Scanner entrada, PrintStream salida, String mensaje, boolean opcional) {
        while (true) {
            salida.print(mensaje);
            String valor = entrada.nextLine().strip();
            if (opcional && valor.isEmpty()) return null;
            try {
                if (valor.matches("[0-9]{4}-[0-9]{2}-[0-9]{2}")) return LocalDate.parse(valor);
            } catch (DateTimeParseException e) {
                // Reintenta sin perder los demás campos.
            }
            salida.println("Ingresa una fecha válida con formato AAAA-MM-DD.");
        }
    }
}