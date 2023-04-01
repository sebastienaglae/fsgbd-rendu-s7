package fr.miage.fsgbd;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.invoke.MethodType;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;

public class Column<TKind extends Comparable<? super TKind>> implements IColumn {
    String name;
    ArrayList<TKind> values;
    transient BPlusTree<TKind, Integer> index;
    int branchingFactor;

    Class<?> type;

    public Column(Class<?> type, String name, int maxValuesPerNode) {
        this.name = name;
        this.values = new ArrayList<>();
        this.type = type;

        enableIndex(maxValuesPerNode);
    }

    public void enableIndex(int maxValuesPerNode) {
        if (maxValuesPerNode > 0) {
            this.branchingFactor = maxValuesPerNode + 1;
            this.index = new BPlusTree<>(branchingFactor);
            for (int i = 0; i < values.size(); i++) {
                index.insert(values.get(i), i);
            }
        } else {
            this.branchingFactor = 0;
            this.index = null;
        }
    }

    public void addValue(TKind value) {
        int row = values.size();
        values.add(value);

        if (index != null) {
            index.insert(value, row);
        }
    }

    public void removeValue(TKind value) {
        values.remove(value);

        if (index != null) {
            enableIndex(branchingFactor);
        }
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void addValue(Object value) {
        addValue((TKind) value);
    }

    @Override
    public void updateValue(int row, Object value) {
        updateValue(row, (TKind) value);
    }

    @Override
    public void removeValue(Object value) {
        removeValue((TKind) value);
    }

    @Override
    public void removeAt(int row) {
        TKind value = values.get(row);
        values.remove(row);

        if (index != null) {
            index.delete(value);
        }
    }

    @Override
    public Object getValue(int row) {
        return values.get(row);
    }

    @Override
    public int size() {
        return values.size();
    }

    @Override
    public int search(Object value) {
        return search((TKind) value);
    }

    @Override
    public boolean contains(Object value) {
        return values.contains((TKind) value);
    }

    @Override
    public void drop() {
        values.clear();
        if (index != null) {
            index = new BPlusTree<>(branchingFactor);
        }
    }

    @Override
    public Class<?> getType() {
        return type;
    }

    @Override
    public boolean isIndexed() {
        return index != null;
    }

    @Override
    public TreeNode toJTree() {
        DefaultMutableTreeNode root = new DefaultMutableTreeNode(name);
        if (index != null) {
            root.add(index.toJTree());
        } else {
            for (Object value : values) {
                root.add(new DefaultMutableTreeNode(value));
            }
        }
        return root;
    }

    public void updateValue(int row, TKind value) {
        TKind oldValue = values.get(row);
        values.set(row, value);

        if (index != null) {
            index.delete(oldValue);
            index.insert(value, row);
        }
    }

    public int search(TKind value) {
        if (index == null) {
            return values.indexOf(value);
        }
        Integer valueRef = index.search(value);
        if (valueRef == null) {
            return -1;
        }
        return valueRef;
    }

    // add callback to deserialize
    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        if (branchingFactor >= 1) {
            index = new BPlusTree<>(branchingFactor);
            for (int i = 0; i < values.size(); i++) {
                index.insert(values.get(i), i);
            }
        }
    }
}

