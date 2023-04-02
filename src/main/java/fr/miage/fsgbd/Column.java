package fr.miage.fsgbd;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serial;
import java.util.ArrayList;

/**
 * Cette classe représente une colonne d'une table de base de données. Elle implémente l'interface IColumn.
 *
 * @param <TKind> le type de données stocké dans la colonne
 */
public class Column<TKind extends Comparable<? super TKind>> implements IColumn {

    /**
     * Le nom de la colonne.
     */
    String name;
    /**
     * Une liste contenant les valeurs stockées dans la colonne.
     */
    ArrayList<TKind> values;
    /**
     * L'index de cette colonne (arbre B+).
     */
    transient BPlusTree<TKind, Integer> index;
    /**
     * Le facteur de ramification de l'arbre B+.
     */
    int branchingFactor;
    /**
     * Le type de données stocké dans la colonne.
     */
    Class<?> type;

    /**
     * Constructeur de la classe Column.
     *
     * @param type             le type de données stocké dans la colonne
     * @param name             le nom de la colonne
     * @param maxValuesPerNode le nombre maximal de valeurs par noeud de l'arbre B+ (si <= 0, désactive l'index)
     */
    public Column(Class<?> type, String name, int maxValuesPerNode) {
        this.name = name;
        this.values = new ArrayList<>();
        this.type = type;

        enableIndex(maxValuesPerNode);
    }

    /**
     * Active l'index de cette colonne avec le facteur de ramification donné en paramètre.
     *
     * @param maxValuesPerNode le nombre maximal de valeurs par noeud de l'arbre B+ (si <= 0, désactive l'index)
     */
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

    /**
     * Ajoute une valeur à la fin de la liste de valeurs de la colonne.
     *
     * @param value la valeur à ajouter
     */
    public void addValue(TKind value) {
        int row = values.size();
        values.add(value);

        if (index != null) {
            index.insert(value, row);
        }
    }

    /**
     * Supprime une valeur de la liste de valeurs de la colonne.
     *
     * @param value la valeur à supprimer
     */
    public void removeValue(TKind value) {
        int row = search(value);
        if (row >= 0) {
            removeAt(row);
        }
    }

    /**
     * Retourne le nom de la colonne.
     *
     * @return le nom de la colonne
     */
    @Override
    public String getName() {
        return name;
    }

    /**
     * Ajoute une valeur à la fin de la liste de valeurs de la colonne.
     *
     * @param value la valeur à ajouter
     */
    @Override
    public void addValue(Object value) {
        addValue((TKind) value);
    }

    /**
     * Met à jour la valeur d'une ligne donnée de la colonne.
     *
     * @param row   la ligne à mettre à jour
     * @param value la nouvelle valeur à assigner
     */
    @Override
    public void updateValue(int row, Object value) {
        updateValue(row, (TKind) value);
    }

    /**
     * Supprime une valeur de la liste de valeurs de la colonne à partir de sa valeur.
     *
     * @param value la valeur à supprimer
     */
    @Override
    public void removeValue(Object value) {
        removeValue((TKind) value);
    }

    /**
     * Supprime une ligne de la colonne à partir de son index.
     *
     * @param row l'index de la ligne à supprimer
     */
    @Override
    public void removeAt(int row) {
        TKind value = values.get(row);
        values.remove(row);

        if (index != null) {
            index.delete(value);
        }
    }

    /**
     * Retourne la valeur d'une ligne donnée de la colonne.
     *
     * @param row la ligne dont on veut récupérer la valeur
     * @return la valeur de la ligne donnée
     */
    @Override
    public Object getValue(int row) {
        return values.get(row);
    }

    /**
     * Retourne le nombre de valeurs dans la colonne.
     *
     * @return le nombre de valeurs dans la colonne
     */
    @Override
    public int size() {
        return values.size();
    }

    /**
     * Recherche l'index de la première occurrence d'une valeur donnée dans la colonne.
     *
     * @param value la valeur à chercher
     * @return l'index de la première occurrence de la valeur donnée, ou -1 si la valeur n'est pas trouvée
     */
    @Override
    public int search(Object value) {
        return search((TKind) value);
    }

    /**
     * Vérifie si la colonne contient une valeur donnée.
     *
     * @param value la valeur à chercher
     * @return true si la colonne contient la valeur donnée, false sinon
     */
    @Override
    public boolean contains(Object value) {
        return values.contains((TKind) value);
    }

    /**
     * Supprime toutes les valeurs de la colonne.
     */
    @Override
    public void drop() {
        values.clear();
        if (index != null) {
            index = new BPlusTree<>(branchingFactor);
        }
    }

    /**
     * Retourne le type de données stocké dans la colonne.
     *
     * @return le type de données stocké dans la colonne
     */
    @Override
    public Class<?> getType() {
        return type;
    }

    /**
     * Vérifie si la colonne est indexée.
     *
     * @return true si la colonne est indexée, false sinon
     */
    @Override
    public boolean isIndexed() {
        return index != null;
    }

    /**
     * Convertit la colonne en un arbre de nœuds Swing pour affichage dans une JTable.
     *
     * @return l'arbre de nœuds Swing correspondant à la colonne
     */
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

    /**
     * Met à jour la valeur d'une ligne donnée de la colonne.
     *
     * @param row   la ligne à mettre à jour
     * @param value la nouvelle valeur à assigner
     */
    public void updateValue(int row, TKind value) {
        TKind oldValue = values.get(row);
        values.set(row, value);

        if (index != null) {
            index.delete(oldValue);
            index.insert(value, row);
        }
    }


    /**
     * Recherche l'index de la première occurrence d'une valeur donnée dans la colonne.
     *
     * @param value la valeur à chercher
     * @return l'index de la première occurrence de la valeur donnée, ou -1 si la valeur n'est pas trouvée
     */
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

    @Serial
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

