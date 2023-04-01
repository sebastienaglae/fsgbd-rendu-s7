package fr.miage.fsgbd;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;

import java.awt.*;
import java.io.IOException;
import java.util.List;

/**
 * @author Galli Gregory, Mopolo Moke Gabriel
 */
public class GUI extends JFrame {
    private Table table;
    private JButton buttonClean, buttonRemove, buttonLoad, buttonSave, buttonAddMany, buttonAddItem, buttonRefresh, buttonBenchmarkWithIndex, buttonBenchmarkWithoutIndex, buttonExportCSV, buttonImportCSV;
    private JTextField txtNbreItem, txtAddRowItem, txtU, txtFile, removeSpecific;
    private final JTree tree = new JTree();

    public GUI() {
        super();
        buildTable(4);
        build();
    }

    private void buildTable(int numValuesPerNode) {
        table = new Table();
        table.addColumn(new Column<Integer>(Integer.class, "ID", numValuesPerNode));
        table.addColumn(new Column<String>(String.class, "Email", numValuesPerNode));
        table.addColumn(new Column<String>(String.class, "Nom", 0));
        table.addColumn(new Column<String>(String.class, "Adresse", 0));
    }

    private void updateTree() {
        DefaultMutableTreeNode root = new DefaultMutableTreeNode("Table Utilisateurs");
        DefaultMutableTreeNode indexedColumnsNode = new DefaultMutableTreeNode("Colonnes indexées");
        DefaultMutableTreeNode unindexedColumnsNode = new DefaultMutableTreeNode("Colonnes non indexées");

        root.add(indexedColumnsNode);
        root.add(unindexedColumnsNode);

        List<IColumn> indexedColumns = table.getColumns().stream().filter(IColumn::isIndexed).toList();
        for (IColumn column : indexedColumns) {
            indexedColumnsNode.add((MutableTreeNode) column.toJTree());
        }

        List<IColumn> unindexedColumns = table.getColumns().stream().filter(c -> !c.isIndexed()).toList();
        for (IColumn column : unindexedColumns) {
            unindexedColumnsNode.add((MutableTreeNode) column.toJTree());
        }

        tree.setModel(new DefaultTreeModel(root));
        for (int i = 0; i < tree.getRowCount(); i++)
            tree.expandRow(i);
        tree.updateUI();
    }

    private void build() {
        setTitle("Indexation - B Arbre");
        setSize(760, 760);
        setLocationRelativeTo(this);
        setResizable(false);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setContentPane(buildContentPane());
    }

    private JPanel buildContentPane() {
        GridBagLayout gLayGlob = new GridBagLayout();

        JPanel pane1 = new JPanel();
        pane1.setLayout(gLayGlob);

        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(0, 5, 2, 0);

        JLabel labelU = new JLabel("Nombre max de clés par noeud (2m): ");
        c.gridx = 0;
        c.gridy = 1;
        c.weightx = 1;
        pane1.add(labelU, c);

        txtU = new JTextField("4", 7);
        c.gridx = 1;
        c.gridy = 1;
        c.weightx = 2;
        pane1.add(txtU, c);

        JLabel labelBetween = new JLabel("Nombre de clés à ajouter:");
        c.gridx = 0;
        c.gridy = 2;
        c.weightx = 1;
        pane1.add(labelBetween, c);

        txtNbreItem = new JTextField("10000", 7);
        c.gridx = 1;
        c.gridy = 2;
        c.weightx = 1;
        pane1.add(txtNbreItem, c);


        buttonAddMany = new JButton("Ajouter n éléments aléatoires à l'arbre");
        c.gridx = 2;
        c.gridy = 2;
        c.weightx = 1;
        c.gridwidth = 2;
        pane1.add(buttonAddMany, c);

        JLabel labelSpecific = new JLabel("Ajouter une valeur spécifique:");
        c.gridx = 0;
        c.gridy = 3;
        c.weightx = 1;
        c.gridwidth = 1;
        pane1.add(labelSpecific, c);

        txtAddRowItem = new JTextField("1,toto@gmail.com,Toto Tata,20 rue de paris", 7);
        c.gridx = 1;
        c.gridy = 3;
        c.weightx = 1;
        c.gridwidth = 1;
        pane1.add(txtAddRowItem, c);

        buttonAddItem = new JButton("Ajouter l'élément");
        c.gridx = 2;
        c.gridy = 3;
        c.weightx = 1;
        c.gridwidth = 2;
        pane1.add(buttonAddItem, c);

        JLabel labelRemoveSpecific = new JLabel("Retirer une valeur spécifique:");
        c.gridx = 0;
        c.gridy = 4;
        c.weightx = 1;
        c.gridwidth = 1;
        pane1.add(labelRemoveSpecific, c);

        removeSpecific = new JTextField("1", 7);
        c.gridx = 1;
        c.gridy = 4;
        c.weightx = 1;
        c.gridwidth = 1;
        pane1.add(removeSpecific, c);

        buttonRemove = new JButton("Supprimer la ligne avec l'ID");
        c.gridx = 2;
        c.gridy = 4;
        c.weightx = 1;
        c.gridwidth = 2;
        pane1.add(buttonRemove, c);

        JLabel labelFilename = new JLabel("Nom de fichier : ");
        c.gridx = 0;
        c.gridy = 5;
        c.weightx = 1;
        c.gridwidth = 1;
        pane1.add(labelFilename, c);

        txtFile = new JTextField("arbre.abr", 7);
        c.gridx = 1;
        c.gridy = 5;
        c.weightx = 1;
        c.gridwidth = 1;
        pane1.add(txtFile, c);

        buttonSave = new JButton("Sauver l'arbre");
        c.gridx = 2;
        c.gridy = 5;
        c.weightx = 0.5;
        c.gridwidth = 1;
        pane1.add(buttonSave, c);

        buttonLoad = new JButton("Charger l'arbre");
        c.gridx = 3;
        c.gridy = 5;
        c.weightx = 0.5;
        c.gridwidth = 1;
        pane1.add(buttonLoad, c);

        buttonClean = new JButton("Reset");
        c.gridx = 2;
        c.gridy = 6;
        c.weightx = 1;
        c.gridwidth = 2;
        pane1.add(buttonClean, c);

        buttonRefresh = new JButton("Refresh");
        c.gridx = 2;
        c.gridy = 7;
        c.weightx = 1;
        c.gridwidth = 2;
        pane1.add(buttonRefresh, c);

        buttonBenchmarkWithIndex = new JButton("Benchmark avec index");
        c.gridx = 2;
        c.gridy = 8;
        c.weightx = 1;
        c.gridwidth = 2;
        pane1.add(buttonBenchmarkWithIndex, c);

        buttonBenchmarkWithoutIndex = new JButton("Benchmark sans index");
        c.gridx = 2;
        c.gridy = 9;
        c.weightx = 1;
        c.gridwidth = 2;
        pane1.add(buttonBenchmarkWithoutIndex, c);

        buttonExportCSV = new JButton("Exporter en CSV");
        c.gridx = 2;
        c.gridy = 10;
        c.weightx = 1;
        c.gridwidth = 2;
        pane1.add(buttonExportCSV, c);

        buttonImportCSV = new JButton("Importer en CSV");
        c.gridx = 2;
        c.gridy = 11;
        c.weightx = 1;
        c.gridwidth = 2;
        pane1.add(buttonImportCSV, c);

        c.fill = GridBagConstraints.HORIZONTAL;
        c.ipady = 400;       //reset to default
        c.weighty = 1.0;   //request any extra vertical space
        c.gridwidth = 4;   //2 columns wide
        c.gridx = 0;
        c.gridy = 12;

        JScrollPane scrollPane = new JScrollPane(tree);
        pane1.add(scrollPane, c);

        updateTree();

        buttonAddItem.addActionListener(e -> {
            String[] rowValueStrings = txtAddRowItem.getText().split(",");
            Object[] rowValues = new Object[rowValueStrings.length];
            for (int i = 0; i < rowValues.length; i++) {
                String value = rowValueStrings[i];
                Object parsedValue = DataGenerator.parseDataForColumn(table.getColumn(i), value);

                rowValues[i] = parsedValue;
            }

            boolean added = table.addRow(rowValues);
            if (!added) {
                List<String> conflictingValues = table.getColumns().stream()
                        .filter(IColumn::isIndexed)
                        .map(IColumn::getName)
                        .toList();

                System.out.println("Tentative d'ajout d'une valeur existante : Conflit avec les indexes de " + conflictingValues);
            }

            updateTree();
        });
        buttonAddMany.addActionListener(e -> {
            int numRows = Integer.parseInt(txtNbreItem.getText());
            for (int i = 0; i < numRows; i++) {
                Object[] row = new Object[table.numColumns()];
                for (int j = 0; j < table.numColumns(); j++) {
                    row[j] = DataGenerator.generateDataForColumn(table.getColumn(j));
                }

                boolean added = table.addRow(row);
                if (!added) {
                    System.out.println("Tentative d'ajout d'une valeur existante : " + row[0]);
                }
            }

            updateTree();
        });
        buttonLoad.addActionListener(e -> {
            FileSerializer<Table> fileSerializer = new FileSerializer<>(txtFile.getText());
            table = fileSerializer.deserialize();

            updateTree();
        });
        buttonSave.addActionListener(e -> {
            FileSerializer<Table> fileSerializer = new FileSerializer<>(txtFile.getText());
            fileSerializer.serialize(table);

            updateTree();
        });
        buttonRemove.addActionListener(e -> {
            String pkString = removeSpecific.getText();
            Object pkValue = DataGenerator.parseDataForColumn(table.getColumn(0), pkString);

            boolean removed = table.removeRow(pkValue);
            if (!removed) {
                System.out.println("Tentative de suppression d'une valeur inexistante : " + pkString);
            }

            updateTree();
        });
        buttonClean.addActionListener(e -> {
            int numValuesPerNode = Integer.parseInt(txtU.getText());
            if (numValuesPerNode < 2)
                System.out.println("Impossible de créer un arbre dont le nombre de clés est inférieur ? 2.");
            else {
                table.drop();
                buildTable(numValuesPerNode);
            }

            updateTree();
        });
        buttonRefresh.addActionListener(e -> tree.updateUI());
        buttonBenchmarkWithIndex.addActionListener(e -> {
            int numValuesPerNode = Integer.parseInt(txtU.getText());
            if (numValuesPerNode < 2) {
                System.out.println("Impossible de benchmark avec un arbre dont le nombre de clés est inférieur ? 2.");
                return;
            }

            benchmark(numValuesPerNode, 1);
            benchmark(numValuesPerNode, 1000);
            benchmark(numValuesPerNode, 50000);
        });
        buttonBenchmarkWithoutIndex.addActionListener(e -> {
            benchmark(0, 1);
            benchmark(0, 1000);
            benchmark(0, 50000);
        });
        buttonExportCSV.addActionListener(e -> {
            CSVExporter.export("arbre.csv", table);
        });
        buttonImportCSV.addActionListener(e -> {
            try {
                table = CSVExporter.importFrom("arbre.csv");
            } catch (IOException ex) {
                ex.printStackTrace();
            }

            updateTree();
        });

        return pane1;
    }

    private void benchmark(int pkIndexMaxValuesPerNode, int numValues) {
        Table table = new Table();
        table.addColumn(new Column<Integer>(Integer.class, "user_id", pkIndexMaxValuesPerNode));
        table.addColumn(new Column<String>(String.class, "name", 0));

        BenchmarkResult insertResult = benchmark(i -> table.addRow(new Object[] { i, "name" + i }), 500);

        System.out.println("Insertion de " + numValues + " valeurs dans une table " + (table.getPkColumn().isIndexed() ? "avec" : "sans") + " index : " + insertResult);

        BenchmarkResult searchResult = benchmark(table::getRowByPK, 500);

        System.out.println("Recherche de " + numValues + " valeurs dans une table " + (table.getPkColumn().isIndexed() ? "avec" : "sans") + " index : " + searchResult);
        System.out.println();
    }

    private BenchmarkResult benchmark(BenchmarkRunnable runnable, long maxMs) {
        long iterationId = 0;
        long iterationCount = 0;
        long startTime = System.nanoTime();

        long iterationMinTime = Long.MAX_VALUE;
        long iterationMaxTime = Long.MIN_VALUE;

        long iterationStartTime;
        long iterationEndTime;
        do {
            iterationStartTime = System.nanoTime();
            int subIteration = 0;
            do {
                runnable.run(iterationId);
                subIteration++;
                iterationCount++;
                iterationEndTime = System.nanoTime();
            } while (iterationEndTime - iterationStartTime < (long) 1000000 * 10);

            iterationId++;

            long iterationDuration = (iterationEndTime - iterationStartTime) / subIteration;

            iterationMinTime = Math.min(iterationMinTime, iterationDuration);
            iterationMaxTime = Math.max(iterationMaxTime, iterationDuration);
        } while (System.nanoTime() - startTime < maxMs * 1000000);
        long endTime = System.nanoTime();
        long duration = (endTime - startTime) / iterationCount;

        return new BenchmarkResult(iterationMinTime, iterationMaxTime, duration);
    }

    interface BenchmarkRunnable {
        void run(long i);
    }

    class BenchmarkResult {
        private final long minTime;
        private final long maxTime;
        private final long avgTime;

        public BenchmarkResult(long minTime, long maxTime, long avgTime) {
            this.minTime = minTime;
            this.maxTime = maxTime;
            this.avgTime = avgTime;
        }

        public long getMinTime() {
            return minTime;
        }

        public long getMaxTime() {
            return maxTime;
        }

        public long getAvgTime() {
            return avgTime;
        }

        @Override
        public String toString() {
            return "min=" + minTime / 1000f + "µs, max=" + maxTime / 1000f + "µs, avg=" + avgTime / 1000f + "µs";
        }
    }
}

