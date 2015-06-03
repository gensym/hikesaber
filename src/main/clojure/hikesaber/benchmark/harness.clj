(ns hikesaber.benchmark.harness
  (:gen-class
   :name org.gensym.hikesaber.benchmark.harness
   :methods [#^{:static true} [countUniqueBikes [Object] int]
             #^{:static true} [loadRecords [] Object]
             ])
  (:require [hikesaber.divvy-ride-records :as records]))

(defn -loadRecords []
  (let [ret records/loaded]
    (count ret) ;; force loading
    ret))

;; Result "countUniqueBikes":
;;   0.666 ±(99.9%) 0.003 s/op [Average]
;;   (min, avg, max) = (0.642, 0.666, 0.732), stdev = 0.013
;;   CI (99.9%): [0.663, 0.670] (assumes normal distribution)
;; 
;; 
;; # Run complete. Total time: 01:02:18
;; 
;; Benchmark                  Mode  Cnt  Score   Error  Units
;; Records.countUniqueBikes  thrpt  200  1.470 ± 0.009  ops/s
;; Records.countUniqueBikes   avgt  200  0.666 ± 0.003   s/op

(defn -countUniqueBikes [records]
  (loop [coll records
         ids #{}]
    (if (empty? coll)
      (count ids)
      (recur (rest coll)
             (conj ids (:bikeid (first coll)))))))




