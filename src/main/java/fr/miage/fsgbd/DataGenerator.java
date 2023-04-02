package fr.miage.fsgbd;

import java.security.SecureRandom;

public class DataGenerator {
    public static Object generateDataForColumn(IColumn column) {
        Class<?> columnType = column.getType();
        Object generatedValue;
        if (columnType == Integer.class) {
            generatedValue = (int) (Math.random() * 100000000);
        } else if (columnType == String.class) {
            generatedValue = generateString(8);
        } else if (columnType == Double.class) {
            generatedValue = Math.random() * 100000000;
        } else if (columnType == Boolean.class) {
            generatedValue = Math.random() > 0.5;
        } else {
            throw new IllegalArgumentException("Unknown column type");
        }

        if (column.isIndexed() && column.contains(generatedValue)) {
            return generateDataForColumn(column);
        }

        return generatedValue;
    }

    public static Object parseDataForColumn(IColumn column, String data) {
        Class<?> columnType = column.getType();
        Object parsedValue;
        if (columnType == Integer.class) {
            parsedValue = Integer.parseInt(data);
        } else if (columnType == String.class) {
            parsedValue = data;
        } else if (columnType == Double.class) {
            parsedValue = Double.parseDouble(data);
        } else if (columnType == Boolean.class) {
            parsedValue = Boolean.parseBoolean(data);
        } else {
            throw new IllegalArgumentException("Unknown column type");
        }

        return parsedValue;
    }

    private static String generateString(int length) {
        String characters = "abcdefghijklmnopqrstuvwxyz0123456789";
        SecureRandom random = new SecureRandom();
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(characters.charAt(random.nextInt(characters.length())));
        }
        return sb.toString();
    }
}
