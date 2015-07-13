(ns hikesaber.benchmark.harness
  (:gen-class
   :name org.gensym.hikesaber.benchmark.harness
   :methods [#^{:static true} [countUniqueBikes [Object] int]
             #^{:static true} [countUniqueBikesTransduce [Object] int]
            #^{:static true} [countUniqueBikesOffHeap [Object] int]
             #^{:static true} [countUniqueBikesOffHeapNth [Object] int]
             #^{:static true} [countUniqueBikesOffHeapTransduce [Object] int]
             #^{:static true} [countUniqueBikesOffHeapTransduceKeyword [Object] int]
             #^{:static true} [loadRecords [] Object]
             #^{:static true} [unloadRecords [Object] Object]
             ])
  (:require [hikesaber.divvy-ride-records :as records]
            [hikesaber.off-heap-ride-records :as ohr]))

;; This can be helpful
(comment (set! *warn-on-reflection* true))

(defn load-records-from [recs]
  (let [record-coll (ohr/make-record-collection recs)]
    {:records recs
     :offheap record-coll}))

(defn -loadRecords []
  (load-records-from records/loaded))

(defn -unloadRecords [records]
  (ohr/dispose (:offheap records)))


;;Result "countUniqueBikes":
;;  0.652 ±(99.9%) 0.004 s/op [Average]
;;  (min, avg, max) = (0.619, 0.652, 0.692), stdev = 0.015
;;  CI (99.9%): [0.648, 0.655] (assumes normal distribution)
(defn -countUniqueBikes [records]
  (loop [i 0
         coll (:records records)
         ids #{}]
    (if (empty? coll)
      (count ids)
      (recur (inc i)
             (rest coll)
             (conj ids (:bikeid (first coll)))))))

;;Result "countUniqueBikesTransduce":
;;  0.701 ±(99.9%) 0.054 s/op [Average]
;;  (min, avg, max) = (0.625, 0.701, 2.637), stdev = 0.228
;;  CI (99.9%): [0.647, 0.755] (assumes normal distribution)
(defn -countUniqueBikesTransduce [records]
  (count (transduce  (map :bikeid) conj #{} (:records records))))

;;Result "countUniqueBikesOffHeap":
;;  0.277 ±(99.9%) 0.006 s/op [Average]
;;  (min, avg, max) = (0.238, 0.277, 0.343), stdev = 0.027
;;  CI (99.9%): [0.270, 0.283] (assumes normal distribution)
(defn -countUniqueBikesOffHeap [records]
  (let [num-records (count (:offheap records))
        unsafe (ohr/unsafe (:offheap records))
        s ohr/object-size]
    (loop [i 0
           offset (ohr/address (:offheap records))
           ids #{}]
      (if (= i num-records)
        (count ids)
        (recur (inc i)
               (+ offset ohr/object-size)
               (conj ids (ohr/get-bike-id unsafe offset)))))))

;;Result "countUniqueBikesOffHeapNth":
;;  0.380 ±(99.9%) 0.005 s/op [Average]
;;  (min, avg, max) = (0.329, 0.380, 0.420), stdev = 0.023
;;  CI (99.9%): [0.375, 0.386] (assumes normal distribution)
(defn -countUniqueBikesOffHeapNth [records]
  (let [num-records (count (:offheap records))
        coll (:offheap records)]
    (loop [i 0
           ids #{}]
      (if (= i num-records)
        (count ids)
        (let [record (nth coll i)]
          (recur (inc i)
                 (conj ids (ohr/bike-id record))))))))

;;Result "countUniqueBikesOffHeapTransduce":
;;  0.319 ±(99.9%) 0.007 s/op [Average]
;;  (min, avg, max) = (0.265, 0.319, 0.378), stdev = 0.031
;;  CI (99.9%): [0.312, 0.326] (assumes normal distribution)
(defn -countUniqueBikesOffHeapTransduce [records]
  (count (transduce (map ohr/bike-id) conj #{} (:offheap records))))

;;Result "countUniqueBikesOffHeapTransduceKeyword":
;;  0.337 ±(99.9%) 0.007 s/op [Average]
;;  (min, avg, max) = (0.297, 0.337, 0.406), stdev = 0.028
;;  CI (99.9%): [0.330, 0.343] (assumes normal distribution)
(defn -countUniqueBikesOffHeapTransduceKeyword [records]
  (count (transduce (map :bikeid) conj #{} (:offheap records))))
