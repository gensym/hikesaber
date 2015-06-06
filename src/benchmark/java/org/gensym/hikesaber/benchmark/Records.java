package org.gensym.hikesaber.benchmark;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.TearDown;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Scope;

import org.gensym.hikesaber.benchmark.harness;

public class Records {
    @State(Scope.Benchmark)
    public static class LoadedRecords {
	volatile Object records;

	@Setup(Level.Trial)
	public void prepare() {
	    records = harness.loadRecords();
	}

	@TearDown(Level.Trial)
	public void check() {
	    harness.unloadRecords(records);
	}
    }

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    public int countUniqueBikes(LoadedRecords records) {
	return harness.countUniqueBikesOffHeap(records.records);
    }
}
