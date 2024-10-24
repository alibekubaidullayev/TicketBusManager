package com.jfb.lecture5;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.*;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class Main {
    private static int totalTickets = 0;
    private static Map<String, Integer> violations = new HashMap<>();

    public static void main(String[] args) throws IOException {
        InputStream is = getFileAsIOStream("ticketData.txt");
        checkTickets(is);
        printValidationResults();
    }

    private static String getInput() {
        return new Scanner(System.in).nextLine();
    }

    private static InputStream getFileAsIOStream(final String fileName) {
        InputStream ioStream = Main.class.getClassLoader().getResourceAsStream(fileName);

        if (ioStream == null) {
            throw new IllegalArgumentException(fileName + " is not found");
        }
        return ioStream;
    }

    private static void checkTickets(InputStream is) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();

        try (InputStreamReader isr = new InputStreamReader(is); BufferedReader br = new BufferedReader(isr)) {
            String line;
            while ((line = br.readLine()) != null) {
                line = fixLine(line);
                Map<String, Object> ticketMap = objectMapper.readValue(line, Map.class);
                boolean valid = validateTicket(ticketMap);
                if (valid) {
                    totalTickets++;
                }
            }
            is.close();
        }
    }

    private static void printValidationResults() {
        System.out.println("");
        System.out.println("Total = " + totalTickets);
        System.out.println("Valid = " + violations.size());
        System.out.println("Most popular violation = " + getMostPopularViolation());
    }

    private static boolean validateTicket(Map<String, Object> ticket) {
        boolean isValid = true;

        try {
            String ticketType = (String) ticket.get("ticketType");
            if (!Arrays.asList("DAY", "WEEK", "MONTH", "YEAR").contains(ticketType)) {
                addViolation("ticket type");
                isValid = false;
            }
        } catch (ClassCastException | NullPointerException e) {
            addViolation("ticket type");
            System.out.println("Error reading ticket type: " + e.getMessage());
            isValid = false;
        }


        try {
            String startDate = (String) ticket.get("startDate");
            if (startDate != null && !startDate.isEmpty()) {
                LocalDate date = LocalDate.parse(startDate);
                if (date.isAfter(LocalDate.now())) {
                    addViolation("start date");
                    isValid = false;
                }
            }
        } catch (DateTimeParseException e) {
            addViolation("start date");
            System.out.println("Error parsing start date: " + e.getMessage());
            isValid = false;
        } catch (ClassCastException | NullPointerException e) {
            addViolation("start date");
            System.out.println("Error reading start date: " + e.getMessage());
            isValid = false;
        }


        try {
            Object priceObj = ticket.get("price");
            int price = Integer.parseInt(priceObj.toString());
            if (price == 0 || price % 2 != 0) {
                addViolation("price");
                isValid = false;
            }
        } catch (NumberFormatException e) {
            addViolation("price");
            System.out.println("Error parsing price: " + e.getMessage());
            isValid = false;
        } catch (ClassCastException | NullPointerException e) {
            addViolation("price");
            System.out.println("Error reading price: " + e.getMessage());
            isValid = false;
        }

        return isValid;
    }

    private static void addViolation(String violationType) {
        violations.put(violationType, violations.getOrDefault(violationType, 0) + 1);
        System.out.println("Violation: " + violationType);
    }

    private static String getMostPopularViolation() {
        return violations.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("No violations");
    }

    private static String fixLine(String in) {
        return in.replace("“", "\"").replace("”", "\"");
    }

}
