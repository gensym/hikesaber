(ns hikesaber.benchmark.harness
  (:gen-class
   :name org.gensym.hikesaber.benchmark.harness
   :methods [#^{:static true} [countUniqueBikes [Object] int]
             #^{:static true} [loadRecords [] Object]
             #^{:static true} [unloadRecords [Object] Object]
             ])
  (:require [hikesaber.divvy-ride-records :as records]
            [hikesaber.off-heap-ride-records :as ohr]))

(defn -loadRecords []
  (let [recs records/loaded
        record-coll (ohr/make-record-collection recs)]
    {:records recs
     :offheap record-coll}))

(defn -unloadRecords [records]
  (ohr/destroy! (:offheap records)))


;;Result "countUniqueBikes":
;;  0.679 ±(99.9%) 0.002 s/op [Average]
;;  (min, avg, max) = (0.656, 0.679, 0.697), stdev = 0.008
;;  CI (99.9%): [0.677, 0.681] (assumes normal distribution)

;;# Run complete. Total time: 01:36:26

;;Benchmark                  Mode  Cnt  Score   Error  Units
;;Records.countUniqueBikes  thrpt  200  1.468 ± 0.005  ops/s
;;Records.countUniqueBikes   avgt  200  0.679 ± 0.002   s/op


(defn -countUniqueBikes [records]
  (loop [coll (:records records)
         ids #{}]
    (if (empty? coll)
      (count ids)
      (recur (rest coll)
             (conj ids (:bikeid (first coll)))))))




