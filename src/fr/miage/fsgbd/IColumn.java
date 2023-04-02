package fr.miage.fsgbd;

import javax.swing.tree.TreeNode;
import java.io.Serializable;

public interface IColumn extends Serializable {
    String getName();

    void enableIndex(int maxValuesPerNode);

    void addValue(Object value);

    void updateValue(int row, Object value);

    void removeValue(Object value);

    void removeAt(int row);

    Object getValue(int row);

    int size();

    int search(Object value);

    boolean contains(Object value);

    void drop();

    Class<?> getType();

    boolean isIndexed();

    TreeNode toJTree();
}
