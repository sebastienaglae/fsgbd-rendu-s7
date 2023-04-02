package fr.miage.fsgbd;

import javax.swing.tree.DefaultMutableTreeNode;
import java.util.*;

public class BPlusTree<K extends Comparable<? super K>, V> {
    /**
     * Nombre à partir duquel on considère qu'un noeud est plein et doit être séparé en deux.
     */
    private final int branchingFactor;
    /**
     * La racine de l'arbre.
     */
    private Node root;

    /**
     * Crée un arbre B+ avec un facteur de branche donné.
     *
     * @param branchingFactor le facteur de branche déclenchant la séparation d'un noeud.
     */
    public BPlusTree(int branchingFactor) {
        if (branchingFactor <= 2)
            throw new IllegalArgumentException("Illegal branching factor: " + branchingFactor);

        this.branchingFactor = branchingFactor;
        root = new LeafNode();
    }

    /**
     * Cherche la valeur associée à une clé.
     *
     * @param key la clé à chercher.
     * @return la valeur associée à la clé, ou null si la clé n'est pas présente dans l'arbre.
     */
    public V search(K key) {
        return root.getValue(key);
    }

    /**
     * Insère une valeur dans l'arbre.
     *
     * @param key   la clé de la valeur à insérer.
     * @param value la valeur à insérer.
     */
    public void insert(K key, V value) {
        root.insertValue(key, value);
    }

    /**
     * Supprime une valeur par sa clé.
     *
     * @param key la clé de la valeur à supprimer.
     */
    public void delete(K key) {
        root.deleteValue(key);
    }

    public DefaultMutableTreeNode toJTree() {
        return toJTree(root);
    }

    public DefaultMutableTreeNode toJTree(Node node) {
        StringBuilder sb = new StringBuilder();
        for (K key : node.keys) {
            if (sb.length() > 0)
                sb.append(' ');

            sb.append("(");
            sb.append(key.toString());
            sb.append(",");
            sb.append(node.getValue(key).toString());
            sb.append(")");
        }

        DefaultMutableTreeNode treeNode = new DefaultMutableTreeNode(sb.toString(), true);
        if (node instanceof BPlusTree.InternalNode) {
            for (Node child : ((InternalNode) node).children) {
                treeNode.add(toJTree(child));
            }
        }

        return treeNode;
    }

    public enum RangePolicy {
        EXCLUSIVE, INCLUSIVE
    }

    /**
     * Cette classe est une classe abstraite représentant un noeud de l'arbre B+.
     */
    private abstract class Node {
        /**
         * Une liste contenant les clés de ce noeud.
         */
        List<K> keys;

        /**
         * Retourne le nombre de clés dans ce noeud.
         *
         * @return le nombre de clés
         */
        int keyNumber() {
            return keys.size();
        }

        /**
         * Retourne la valeur associée à la clé donnée en paramètre.
         *
         * @param key la clé pour laquelle récupérer la valeur
         * @return la valeur associée à la clé
         */
        abstract V getValue(K key);

        /**
         * Supprime la valeur associée à la clé donnée en paramètre.
         *
         * @param key la clé pour laquelle supprimer la valeur
         */
        abstract void deleteValue(K key);

        /**
         * Insère une clé et sa valeur associée dans le noeud.
         *
         * @param key   la clé à insérer
         * @param value la valeur associée à la clé à insérer
         */
        abstract void insertValue(K key, V value);

        /**
         * Retourne la première clé de feuille dans ce noeud.
         *
         * @return la première clé de feuille
         */
        abstract K getFirstLeafKey();

        /**
         * Retourne une liste de valeurs associées aux clés qui sont comprises entre les deux clés données en paramètre.
         *
         * @param key1    la première clé
         * @param policy1 la politique à appliquer pour inclure ou exclure la première clé
         * @param key2    la deuxième clé
         * @param policy2 la politique à appliquer pour inclure ou exclure la deuxième clé
         * @return une liste de valeurs associées aux clés comprises entre les deux clés données
         */
        abstract List<V> getRange(K key1, RangePolicy policy1, K key2,
                                  RangePolicy policy2);

        /**
         * Fusionne ce noeud avec son frère.
         *
         * @param sibling le frère avec lequel fusionner
         */
        abstract void merge(Node sibling);

        /**
         * Divise ce noeud en deux noeuds.
         *
         * @return le noeud créé lors de la division
         */
        abstract Node split();

        /**
         * Retourne true si ce noeud est en surcharge, c'est-à-dire qu'il contient trop de clés.
         *
         * @return true si le noeud est en surcharge
         */
        abstract boolean isOverflow();

        /**
         * Retourne true si ce noeud est en sous-charge, c'est-à-dire qu'il contient trop peu de clés.
         *
         * @return true si le noeud est en sous-charge
         */
        abstract boolean isUnderflow();

        /**
         * Retourne une représentation sous forme de chaîne de caractères de ce noeud, contenant toutes ses clés.
         *
         * @return une représentation sous forme de chaîne de caractères de ce noeud
         */
        public String toString() {
            return keys.toString();
        }
    }

    /**
     * Cette classe représente un noeud interne de l'arbre B+.
     * Chaque noeud interne possède une liste de clés et une liste de noeuds enfants.
     * Les clés sont utilisées pour naviguer dans l'arbre, tandis que les enfants sont des noeuds
     * qui permettent d'atteindre les valeurs correspondantes aux clés.
     */
    private class InternalNode extends Node {
        /**
         * Une liste contenant les clés de ce noeud.
         */
        List<Node> children;

        /**
         * Construit un nouveau noeud interne.
         */
        InternalNode() {
            this.keys = new ArrayList<K>();
            this.children = new ArrayList<Node>();
        }

        @Override
        V getValue(K key) {
            return getChild(key).getValue(key);
        }

        @Override
        void deleteValue(K key) {
            Node child = getChild(key);
            child.deleteValue(key);
            if (child.isUnderflow()) {
                Node childLeftSibling = getChildLeftSibling(key);
                Node childRightSibling = getChildRightSibling(key);
                Node left = childLeftSibling != null ? childLeftSibling : child;
                Node right = childLeftSibling != null ? child : childRightSibling;
                left.merge(right);
                deleteChild(right.getFirstLeafKey());
                if (left.isOverflow()) {
                    Node sibling = left.split();
                    insertChild(sibling.getFirstLeafKey(), sibling);
                }
                if (root.keyNumber() == 0)
                    root = left;
            }
        }

        @Override
        void insertValue(K key, V value) {
            Node child = getChild(key);
            child.insertValue(key, value);
            if (child.isOverflow()) {
                Node sibling = child.split();
                insertChild(sibling.getFirstLeafKey(), sibling);
            }
            if (root.isOverflow()) {
                Node sibling = split();
                InternalNode newRoot = new InternalNode();
                newRoot.keys.add(sibling.getFirstLeafKey());
                newRoot.children.add(this);
                newRoot.children.add(sibling);
                root = newRoot;
            }
        }

        @Override
        K getFirstLeafKey() {
            return children.get(0).getFirstLeafKey();
        }

        @Override
        List<V> getRange(K key1, RangePolicy policy1, K key2,
                         RangePolicy policy2) {
            return getChild(key1).getRange(key1, policy1, key2, policy2);
        }

        @Override
        void merge(Node sibling) {
            @SuppressWarnings("unchecked")
            InternalNode node = (InternalNode) sibling;
            keys.add(node.getFirstLeafKey());
            keys.addAll(node.keys);
            children.addAll(node.children);

        }

        @Override
        Node split() {
            int from = keyNumber() / 2 + 1, to = keyNumber();
            InternalNode sibling = new InternalNode();
            sibling.keys.addAll(keys.subList(from, to));
            sibling.children.addAll(children.subList(from, to + 1));

            keys.subList(from - 1, to).clear();
            children.subList(from, to + 1).clear();

            return sibling;
        }

        @Override
        boolean isOverflow() {
            return children.size() > branchingFactor;
        }

        @Override
        boolean isUnderflow() {
            return children.size() < (branchingFactor + 1) / 2;
        }

        /**
         * Retourne l'enfant correspondant à la clé donnée en paramètre.
         *
         * @param key la clé pour laquelle récupérer l'enfant
         * @return l'enfant correspondant à la clé
         */
        Node getChild(K key) {
            int loc = Collections.binarySearch(keys, key);
            int childIndex = loc >= 0 ? loc + 1 : -loc - 1;
            return children.get(childIndex);
        }

        /**
         * Supprime l'enfant correspondant à la clé donnée en paramètre.
         *
         * @param key la clé pour laquelle supprimer l'enfant
         */
        void deleteChild(K key) {
            int loc = Collections.binarySearch(keys, key);
            if (loc >= 0) {
                keys.remove(loc);
                children.remove(loc + 1);
            }
        }

        /**
         * Insère un enfant associé à une clé dans ce noeud.
         *
         * @param key   la clé associée à l'enfant à insérer
         * @param child l'enfant à insérer
         */
        void insertChild(K key, Node child) {
            int loc = Collections.binarySearch(keys, key);
            int childIndex = loc >= 0 ? loc + 1 : -loc - 1;
            if (loc >= 0) {
                children.set(childIndex, child);
            } else {
                keys.add(childIndex, key);
                children.add(childIndex + 1, child);
            }
        }

        /**
         * Retourne l'enfant à gauche de celui correspondant à la clé donnée en paramètre.
         *
         * @param key la clé pour laquelle récupérer l'enfant à gauche
         * @return l'enfant à gauche de celui correspondant à la clé
         */
        Node getChildLeftSibling(K key) {
            int loc = Collections.binarySearch(keys, key);
            int childIndex = loc >= 0 ? loc + 1 : -loc - 1;
            if (childIndex > 0)
                return children.get(childIndex - 1);

            return null;
        }

        /**
         * Retourne l'enfant à droite de celui correspondant à la clé donnée en paramètre.
         *
         * @param key la clé pour laquelle récupérer l'enfant à droite
         * @return l'enfant à droite de celui correspondant à la clé
         */
        Node getChildRightSibling(K key) {
            int loc = Collections.binarySearch(keys, key);
            int childIndex = loc >= 0 ? loc + 1 : -loc - 1;
            if (childIndex < keyNumber())
                return children.get(childIndex + 1);

            return null;
        }
    }

    /**
     * Cette classe représente un noeud feuille de l'arbre B+. Elle hérite de la classe abstraite Node.
     */
    private class LeafNode extends Node {
        /**
         * Une liste contenant les valeurs associées aux clés de ce noeud.
         */
        List<V> values;

        /**
         * Le noeud feuille suivant dans la séquence de feuilles.
         */
        LeafNode next;

        /**
         * Constructeur de la classe LeafNode. Initialise les listes de clés et de valeurs.
         */
        LeafNode() {
            keys = new ArrayList<K>();
            values = new ArrayList<V>();
        }

        @Override
        V getValue(K key) {
            int loc = Collections.binarySearch(keys, key);
            return loc >= 0 ? values.get(loc) : null;
        }

        @Override
        void deleteValue(K key) {
            int loc = Collections.binarySearch(keys, key);
            if (loc >= 0) {
                keys.remove(loc);
                values.remove(loc);
            }
        }

        @Override
        void insertValue(K key, V value) {
            int loc = Collections.binarySearch(keys, key);
            int valueIndex = loc >= 0 ? loc : -loc - 1;
            if (loc >= 0) {
                values.set(valueIndex, value);
            } else {
                keys.add(valueIndex, key);
                values.add(valueIndex, value);
            }
            if (root.isOverflow()) {
                Node sibling = split();
                InternalNode newRoot = new InternalNode();
                newRoot.keys.add(sibling.getFirstLeafKey());
                newRoot.children.add(this);
                newRoot.children.add(sibling);
                root = newRoot;
            }
        }

        @Override
        K getFirstLeafKey() {
            return keys.get(0);
        }

        @Override
        List<V> getRange(K key1, RangePolicy policy1, K key2,
                         RangePolicy policy2) {
            List<V> result = new LinkedList<V>();
            LeafNode node = this;
            while (node != null) {
                Iterator<K> kIt = node.keys.iterator();
                Iterator<V> vIt = node.values.iterator();
                while (kIt.hasNext()) {
                    K key = kIt.next();
                    V value = vIt.next();
                    int cmp1 = key.compareTo(key1);
                    int cmp2 = key.compareTo(key2);
                    if (((policy1 == RangePolicy.EXCLUSIVE && cmp1 > 0) || (policy1 == RangePolicy.INCLUSIVE && cmp1 >= 0))
                            && ((policy2 == RangePolicy.EXCLUSIVE && cmp2 < 0) || (policy2 == RangePolicy.INCLUSIVE && cmp2 <= 0)))
                        result.add(value);
                    else if ((policy2 == RangePolicy.EXCLUSIVE && cmp2 >= 0)
                            || (policy2 == RangePolicy.INCLUSIVE && cmp2 > 0))
                        return result;
                }
                node = node.next;
            }
            return result;
        }

        @Override
        void merge(Node sibling) {
            @SuppressWarnings("unchecked")
            LeafNode node = (LeafNode) sibling;
            keys.addAll(node.keys);
            values.addAll(node.values);
            next = node.next;
        }

        @Override
        Node split() {
            LeafNode sibling = new LeafNode();
            int from = (keyNumber() + 1) / 2, to = keyNumber();
            sibling.keys.addAll(keys.subList(from, to));
            sibling.values.addAll(values.subList(from, to));

            keys.subList(from, to).clear();
            values.subList(from, to).clear();

            sibling.next = next;
            next = sibling;
            return sibling;
        }

        @Override
        boolean isOverflow() {
            return values.size() > branchingFactor - 1;
        }

        @Override
        boolean isUnderflow() {
            return values.size() < branchingFactor / 2;
        }
    }
}