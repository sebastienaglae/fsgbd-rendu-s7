package fr.miage.fsgbd.benchmark;

import fr.miage.fsgbd.BPlusTree;
import fr.miage.fsgbd.Column;
import fr.miage.fsgbd.Table;
import org.openjdk.jmh.annotations.*;

import java.util.concurrent.TimeUnit;

@State(Scope.Benchmark)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@Warmup(iterations = 3, time = 1)
@Measurement(iterations = 3, time = 1)
@Fork(3)
@BenchmarkMode(Mode.All)
public class BTreeBenchmark {
    @Param ({"100", "1000", "10000"})
    public int size;

    private Table nonIndexedTable;
    private Table indexedTable;

    @Setup(Level.Iteration)
    public void setup() {
        nonIndexedTable = new Table();
        nonIndexedTable.addColumn(new Column<Integer>(Integer.class, "id", 0));

        indexedTable = new Table();
        indexedTable.addColumn(new Column<Integer>(Integer.class, "id", 4));

        for (int i = 0; i < size; i++) {
            nonIndexedTable.addRow(new Object[]{i});
            indexedTable.addRow(new Object[]{i});
        }
    }

    @TearDown(Level.Invocation)
    public void tearDown() {
        if (nonIndexedTable.numRows() > size) {
            nonIndexedTable.removeRow(nonIndexedTable.numRows() - 1);
        }
        if (indexedTable.numRows() > size) {
            indexedTable.removeRow(indexedTable.numRows() - 1);
        }
    }


    @Benchmark
    public void insert() {
        nonIndexedTable.addRow(new Object[]{nonIndexedTable.numRows()});
    }

    @Benchmark
    public void insertBTree() {
        indexedTable.addRow(new Object[]{indexedTable.numRows()});
    }

    @Benchmark
    public void selectFirst() {
        nonIndexedTable.getRowByPK(0);
    }

    @Benchmark
    public void selectFirstBTree() {
        indexedTable.getRowByPK(0);
    }

    @Benchmark
    public void selectLast() {
        nonIndexedTable.getRowByPK(nonIndexedTable.numRows() - 1);
    }

    @Benchmark
    public void selectLastBTree() {
        indexedTable.getRowByPK(indexedTable.numRows() - 1);
    }
}

