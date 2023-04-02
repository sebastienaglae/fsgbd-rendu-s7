package fr.miage.fsgbd;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Cette classe représente une table qui contient des colonnes avec des données. Elle implémente l'interface Serializable.
 */
public class Table implements Serializable {

    /**
     * Les colonnes de la table.
     */
    private final ArrayList<IColumn> columns;

    /**
     * Constructeur par défaut.
     * Initialise les colonnes de la table avec une ArrayList vide.
     */
    public Table() {
        columns = new ArrayList<>();
    }

    /**
     * Ajoute une colonne à la table.
     *
     * @param column la colonne à ajouter.
     */
    public void addColumn(IColumn column) {
        columns.add(column);

        int numRows = numRows();
        if (numRows > 0) {
            for (int i = 0; i < numRows; i++) {
                column.addValue(null);
            }
        }
    }

    /**
     * Ajoute une nouvelle ligne à la table avec les valeurs spécifiées.
     *
     * @param row le tableau d'objets représentant la nouvelle ligne à ajouter.
     * @return true si la ligne a été ajoutée avec succès, false si une colonne indexée contient déjà la valeur d'une cellule de la ligne.
     * @throws IllegalArgumentException si la taille du tableau ne correspond pas au nombre de colonnes de la table.
     */
    public boolean addRow(Object[] row) throws IllegalArgumentException {
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

    /**
     * Récupère une ligne de la table.
     *
     * @param row l'indice de la ligne à récupérer.
     * @return un tableau d'objets représentant la ligne de la table.
     */
    private Object[] getRow(int row) {
        Object[] result = new Object[columns.size()];
        for (int i = 0; i < columns.size(); i++) {
            result[i] = columns.get(i).getValue(row);
        }
        return result;
    }

    /**
     * Récupère une ligne de la table à partir de sa clé primaire.
     *
     * @param pk la clé primaire de la ligne à récupérer.
     * @return un tableau d'objets représentant la ligne de la table, ou null si aucune ligne ne correspond à la clé primaire spécifiée.
     */
    public Object[] getRowByPK(Object pk) {
        int row = getRowIndexByPK(pk);
        if (row == -1) {
            return null;
        }
        return getRow(row);
    }

    /**
     * Supprime une ligne de la table à partir de sa clé primaire.
     *
     * @param pk la clé primaire de la ligne à supprimer.
     * @return true si la ligne a été supprimée avec succès, false sinon.
     */
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

    /**
     * Récupère l'indice d'une ligne de la table à partir de sa clé primaire.
     *
     * @param pk la clé primaire de la ligne à récupérer.
     * @return l'indice de la ligne, ou -1 si aucune ligne ne correspond à la clé primaire spécifiée.
     */
    public int getRowIndexByPK(Object pk) {
        IColumn pkColumn = getPkColumn();
        int row = pkColumn.search(pk);
        return row;
    }

    /**
     * Récupère le nombre de lignes de la table.
     *
     * @return le nombre de lignes de la table.
     */
    public int numRows() {
        if (columns.size() == 0) {
            return 0;
        }
        return getPkColumn().size();
    }

    /**
     * Récupère le nombre de colonnes de la table.
     *
     * @return le nombre de colonnes de la table.
     */
    public int numColumns() {
        return columns.size();
    }

    /**
     * Récupère une colonne de la table.
     *
     * @param index l'indice de la colonne à récupérer.
     * @return la colonne de la table.
     */
    public IColumn getColumn(int index) {
        return columns.get(index);
    }

    /**
     * Récupère la colonne de clé primaire de la table.
     *
     * @return la colonne de clé primaire de la table.
     */
    public IColumn getPkColumn() {
        return columns.get(0);
    }

    /**
     * Récupère le nom de la table.
     *
     * @return le nom de la table.
     */
    public List<IColumn> getColumns() {
        return columns;
    }

    /**
     * Supprime toutes les lignes de la table.
     */
    public void drop() {
        for (IColumn column : columns) {
            column.drop();
        }
    }
}

