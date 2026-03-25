package com.example;

import com.example.weather.localization.WeatherDataQueryDTO;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Optional;
import java.util.Scanner;
//todo: delete

public class Interface {
    public static void main(String[] args) {


        Scanner scanner = new Scanner(System.in);
        System.out.println("Witaj w aplikacji pogodowej!");
        ObjectMapper objectMapper = new ObjectMapper();
        while (true) {
            System.out.println("\n");
            Cl.printlnC(Cl.BLUEs, 2, "Co chcesz zrobić:");
            System.out.println(" ");
            System.out.println("1. Dodaj miasto");
            System.out.println("2. Wyświetl listę zapisanych miast");
            System.out.println("3. Sprawdź pogodę dla wybranego miasta");
            Cl.printlnC(Cl.REDs, 2, "0. Wyłącz program");
            Cl.printlnC(Cl.BLUEs, 2, "Twój wybór: ");
            int value = readMenuChoice(scanner);

            switch (value) {
                case 1:
                    Cl.printlnC(Cl.YELLOWs, 2, "Podaj nazwę miasta:");
                    String city = scanner.nextLine();
                    Cl.printlnC(Cl.YELLOWs, 2, "Podaj kraj:");
                    String country = scanner.nextLine();
                    Cl.printlnC(Cl.YELLOWs, 2, "Podaj region (lub wciśnij enter):");
                    String region = scanner.nextLine();
                    double latitude = readDouble(scanner, "Podaj szerokość geograficzną: ");
                    double longitude = readDouble(scanner, "Podaj długość geograficzną: ");

                    String json = "{\"city\":\"" + city + "\",\"country\":\"" + country + "\",\"region\":\"" + region + "\",\"latitude\":" + latitude + ",\"longitude\":" + longitude + "}";
                    Cl.printlnC(Cl.BLUEs, 2, "\nZorbione - odpowiedź: " );
                    break;
                case 2:


                    break;
                case 3:

                    break;

                case 0:
                    Cl.printlnC(Cl.BLUEs, 2, "Do widzenia!");
                    return;
            }
        }
    }

    private static int readMenuChoice(Scanner scanner) {
        while (true) {
            String line = scanner.nextLine().trim();
            try {
                int value = Integer.parseInt(line);
                if (value >= 0 && value <= 3) return value;
            } catch (NumberFormatException ignored) {
            }
            System.out.println("Podaj numer opcji (0-3).");
            System.out.print("Twój wybór: ");
        }
    }

    private static double readDouble(Scanner scanner, String prompt) {
        while (true) {
            System.out.print(prompt);
            String line = scanner.nextLine().trim().replace(",", ".");
            try {
                return Double.parseDouble(line);
            } catch (NumberFormatException e) {
                System.out.println("Podaj liczbę (np. 51.107 lub 51,107).");
            }
        }
    }

    private static long readLong(Scanner scanner, String prompt) {
        while (true) {
            System.out.print(prompt);
            String line = scanner.nextLine().trim();
            try {
                return Long.parseLong(line);
            } catch (NumberFormatException e) {
                System.out.println("Podaj liczbę całkowitą.");
            }
        }
    }

}
