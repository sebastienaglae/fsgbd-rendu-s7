package fr.miage.fsgbd;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

public class Table implements Serializable {
    private ArrayList<IColumn> columns;

    public Table() {
        columns = new ArrayList<>();
    }

    public void addColumn(IColumn column) {
        columns.add(column);

        int numRows = numRows();
        if (numRows > 0) {
            for (int i = 0; i < numRows; i++) {
                column.addValue(null);
            }
        }
    }

    public boolean addRow(Object[] row) {
        if (row.length != columns.size()) {
            StringBuilder attemptedTypes = new StringBuilder();
            for (IColumn column : columns) {
                if (attemptedTypes.length() > 0) {
                    attemptedTypes.append(", ");
                }
                attemptedTypes.append(column.getType().getSimpleName());
            }
            throw new IllegalArgumentException("Row size does not match table size. Attempted types: " + attemptedTypes);
        }
        for (int i = 0; i < row.length; i++) {
            IColumn column = columns.get(i);
            if (column.isIndexed() && column.contains(row[i])) {
                // L'index ne supporte pas les doublons
                return false;
            }
        }

        for (int i = 0; i < row.length; i++) {
            columns.get(i).addValue(row[i]);
        }

        return true;
    }

    private Object[] getRow(int row) {
        Object[] result = new Object[columns.size()];
        for (int i = 0; i < columns.size(); i++) {
            result[i] = columns.get(i).getValue(row);
        }
        return result;
    }

    public Object[] getRowByPK(Object pk) {
        int row = getRowIndexByPK(pk);
        if (row == -1) {
            return null;
        }
        return getRow(row);
    }
    public boolean removeRow(Object pk) {
        int row = getRowIndexByPK(pk);
        if (row == -1) {
            return false;
        }
        for (IColumn column : columns) {
            column.removeAt(row);
        }

        return true;
    }

    public int getRowIndexByPK(Object pk) {
        IColumn pkColumn = getPkColumn();
        int row = pkColumn.search(pk);
        return row;
    }

    public int numRows() {
        if (columns.size() == 0) {
            return 0;
        }
        return getPkColumn().size();
    }

    public int numColumns() {
        return columns.size();
    }

    public IColumn getColumn(int index) {
        return columns.get(index);
    }

    public IColumn getPkColumn() {
        return columns.get(0);
    }

    public List<IColumn> getColumns() {
        return columns;
    }

    public void drop() {
        for (IColumn column : columns) {
            column.drop();
        }
    }
}

