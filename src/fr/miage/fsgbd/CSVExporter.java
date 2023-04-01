package fr.miage.fsgbd;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;

public class CSVExporter {
    public static void export(String filename, Table table) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < table.numColumns(); i++) {
            if (i > 0)
                sb.append(',');
            sb.append(table.getColumn(i).getName());
        }
        sb.append('\n');

        for (int i = 0; i < table.numColumns(); i++) {
            if (i > 0)
                sb.append(',');

            String columnType = table.getColumn(i).getType().getSimpleName();
            if (getType(columnType) == null) {
                throw new IllegalArgumentException("Export failed. Unsupported column type: " + columnType);
            }

            sb.append(columnType);
        }
        sb.append('\n');

        int numRows = table.numRows();
        for (int i = 0; i < numRows; i++) {
            for (int j = 0; j < table.numColumns(); j++) {
                if (j > 0)
                    sb.append(',');
                sb.append(table.getColumn(j).getValue(i));
            }
            sb.append('\n');
        }

        try {
            Files.writeString(Path.of(filename), sb.toString());
        } catch (IOException e1) {
            e1.printStackTrace();
        }
    }

    public static Table importFrom(String filename) throws IOException {
        Table table = new Table();

        String content = Files.readString(Path.of(filename)).replace("\r\n", "\n");
        String[] lines = content.split("\n");
        if (lines.length < 2) {
            throw new IllegalArgumentException("Invalid file format. File must contain at least 2 lines.");
        }
        String[] columnNames = parseLine(lines[0]);
        String[] columnTypes = parseLine(lines[1]);
        if (columnNames.length != columnTypes.length) {
            throw new IllegalArgumentException("Invalid file format. Column names and types must have the same length.");
        }

        for (int i = 0; i < columnNames.length; i++) {
            String columnName = columnNames[i];
            String columnType = columnTypes[i];
            Type javaType = getType(columnType);
            if (javaType == null) {
                throw new IllegalArgumentException("Invalid file format. Unknown column type: " + columnType);
            }
            if (javaType == Integer.class) {
                table.addColumn(new Column<Integer>(Integer.class, columnName, 0));
            } else if (javaType == String.class) {
                table.addColumn(new Column<String>(String.class, columnName, 0));
            } else if (javaType == Double.class) {
                table.addColumn(new Column<Double>(Double.class, columnName, 0));
            } else if (javaType == Boolean.class) {
                table.addColumn(new Column<Boolean>(Boolean.class, columnName, 0));
            } else {
                throw new IllegalArgumentException("Invalid file format. Unknown column type: " + columnType);
            }
        }

        table.getPkColumn().enableIndex(4);

        for (int i = 2; i < lines.length; i++) {
            String[] values = parseLine(lines[i]);
            if (values.length != columnNames.length) {
                throw new IllegalArgumentException("Invalid file format. Row " + i + " does not have the same number of columns as the header.");
            }
            Object[] row = new Object[columnNames.length];
            for (int j = 0; j < columnNames.length; j++) {
                Object value = DataGenerator.parseDataForColumn(table.getColumn(j), values[j]);
                row[j] = value;
            }

            boolean success = table.addRow(row);
            if (!success) {
                throw new IllegalArgumentException("Invalid file format. Duplicate primary key.");
            }
        }

        return table;
    }

    private static String[] parseLine(String line) {
        ArrayList<String> result = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        boolean inQuotes = false;
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '"') {
                inQuotes = !inQuotes;
            } else if (c == ',' && !inQuotes) {
                result.add(sb.toString());
                sb.setLength(0);
            } else {
                sb.append(c);
            }
        }
        result.add(sb.toString());
        return result.toArray(new String[0]);
    }

    private static Type getType(String type) {
        switch (type) {
            case "Integer":
                return Integer.class;
            case "String":
                return String.class;
            case "Double":
                return Double.class;
            case "Boolean":
                return Boolean.class;
            default:
                throw new IllegalArgumentException("Invalid file format. Unknown column type: " + type);
        }
    }
}