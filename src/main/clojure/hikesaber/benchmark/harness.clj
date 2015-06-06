(ns hikesaber.benchmark.harness
  (:gen-class
   :name org.gensym.hikesaber.benchmark.harness
   :methods [#^{:static true} [countUniqueBikes [Object] int]
             #^{:static true} [countUniqueBikesOffHeap [Object] int]
             #^{:static true} [loadRecords [] Object]
             #^{:static true} [unloadRecords [Object] Object]
             ])
  (:require [hikesaber.divvy-ride-records :as records]
            [hikesaber.off-heap-ride-records :as ohr]))

(defn load-records-from [recs]
  (let [record-coll (ohr/make-record-collection recs)]
    (println "Loading...")
    {:records recs
     :offheap record-coll}))

(defn -loadRecords []
  (load-records-from records/loaded))



(defn -unloadRecords [records]
  (println "Unloading...")
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
  (loop [i 0
         coll (:records records)
         ids #{}]
    (if (empty? coll)
      (count ids)
      (recur (inc i)
             (rest coll)
             (conj ids (:bikeid (first coll)))))))



;;Result "countUniqueBikes":
;;  0.289 ±(99.9%) 0.005 s/op [Average]
;;  (min, avg, max) = (0.252, 0.289, 0.343), stdev = 0.020
;;  CI (99.9%): [0.284, 0.294] (assumes normal distribution)
;;
;;
;;# Run complete. Total time: 01:00:53
;;
;;Benchmark                  Mode  Cnt  Score   Error  Units
;;Records.countUniqueBikes  thrpt  200  3.502 ± 0.073  ops/s
;;Records.countUniqueBikes   avgt  200  0.289 ± 0.005   s/op

(defn -countUniqueBikesOffHeap [records]
  (let [num-records (:count (:offheap records))
        unsafe (:unsafe (:offheap records))
        s ohr/object-size]
    (loop [i 0
           offset (:address (:offheap records))
           ids #{}]
      (if (= i num-records)
        (count ids)
        (recur (inc i)
               (+ offset ohr/object-size)
               (conj ids (ohr/get-bike-id unsafe offset)))))))




