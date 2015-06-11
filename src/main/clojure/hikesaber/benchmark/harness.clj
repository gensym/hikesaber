(ns hikesaber.benchmark.harness
  (:gen-class
   :name org.gensym.hikesaber.benchmark.harness
   :methods [#^{:static true} [countUniqueBikes [Object] int]
             #^{:static true} [countUniqueBikesTransduce [Object] int]
             #^{:static true} [countUniqueBikesOffHeap [Object] int]
             #^{:static true} [countUniqueBikesOffHeapNth [Object] int]
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
  (ohr/destroy! (:offheap records)))



;;Result "countUniqueBikes":
;;  0.658 ±(99.9%) 0.004 s/op [Average]
;;  (min, avg, max) = (0.623, 0.658, 0.696), stdev = 0.016
;;  CI (99.9%): [0.654, 0.662] (assumes normal distribution)
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
;;  0.654 ±(99.9%) 0.004 s/op [Average]
;;  (min, avg, max) = (0.618, 0.654, 0.710), stdev = 0.018
;;  CI (99.9%): [0.650, 0.658] (assumes normal distribution)
(defn -countUniqueBikesTransduce [records]
  (count (transduce  (map :bikeid) conj #{} (:records records))))


;;Result "countUniqueBikesOffHeap":
;;  0.289 ±(99.9%) 0.006 s/op [Average]
;;  (min, avg, max) = (0.261, 0.289, 0.349), stdev = 0.023
;;  CI (99.9%): [0.284, 0.295] (assumes normal distribution)
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


;;Result "countUniqueBikesOffHeapNth":
;;  0.476 ±(99.9%) 0.006 s/op [Average]
;;  (min, avg, max) = (0.428, 0.476, 0.555), stdev = 0.028
;;  CI (99.9%): [0.469, 0.482] (assumes normal distribution)
(defn -countUniqueBikesOffHeapNth [records]
  (let [num-records (:count (:offheap records))
        coll (:offheap records)]
    (loop [i 0
           ids #{}]
      (if (= i num-records)
        (count ids)
        (let [record (ohr/nth coll i)]
          (recur (inc i)
                 (conj ids (ohr/get-bike-id record))))))))

