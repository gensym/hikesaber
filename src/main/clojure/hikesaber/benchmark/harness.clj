(ns hikesaber.benchmark.harness
  (:gen-class
   :name org.gensym.hikesaber.benchmark.harness
   :methods [#^{:static true} [countUniqueBikes [Object] int]
             #^{:static true} [loadRecords [] Object]
             ])
  (:require [hikesaber.divvy-ride-records :as records]))

(defn -loadRecords [] records/loaded)


;;Result "countUniqueBikes":
;;  0.658 ±(99.9%) 0.003 s/op [Average]
;;  (min, avg, max) = (0.633, 0.658, 0.731), stdev = 0.015
;;  CI (99.9%): [0.655, 0.662] (assumes normal distribution)
;;
;;
;;# Run complete. Total time: 01:02:55
;;
;;Benchmark                  Mode  Cnt  Score   Error  Units
;;Records.countUniqueBikes  thrpt  200  1.491 ± 0.008  ops/s
;;Records.countUniqueBikes   avgt  200  0.658 ± 0.003   s/op
(defn -countUniqueBikes [records]
  (loop [coll records
         ids #{}]
    (if (empty? coll)
      (count ids)
      (recur (rest coll)
             (conj ids (:bikeid (first coll)))))))




