(ns hikesaber.benchmark.harness
  (:gen-class
   :name org.gensym.hikesaber.benchmark.harness
   :methods [#^{:static true} [countUniqueBikes [Object] int]
             #^{:static true} [countUniqueBikesTransduce [Object] int]
             #^{:static true} [countUniqueBikesOffHeap [Object] int]
             #^{:static true} [countUniqueBikesOffHeapNth [Object] int]
             #^{:static true} [countUniqueBikesOffHeapTransduce [Object] int]
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
;;  0.273 ±(99.9%) 0.005 s/op [Average]
;;  (min, avg, max) = (0.253, 0.273, 0.337), stdev = 0.020
;;  CI (99.9%): [0.268, 0.278] (assumes normal distribution)
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
;;  0.404 ±(99.9%) 0.006 s/op [Average]
;;  (min, avg, max) = (0.372, 0.404, 0.474), stdev = 0.026
;;  CI (99.9%): [0.398, 0.410] (assumes normal distribution)
(defn -countUniqueBikesOffHeapNth [records]
  (let [num-records (count (:offheap records))
        coll (:offheap records)]
    (loop [i 0
           ids #{}]
      (if (= i num-records)
        (count ids)
        (let [record (nth coll i)]
          (recur (inc i)
                 (conj ids (ohr/get-bike-id record))))))))


;;Result "countUniqueBikesOffHeapTransduce":
;;  0.307 ±(99.9%) 0.006 s/op [Average]
;;  (min, avg, max) = (0.274, 0.307, 0.370), stdev = 0.026
;;  CI (99.9%): [0.301, 0.313] (assumes normal distribution)
(defn -countUniqueBikesOffHeapTransduce [records]
  (count (transduce (map ohr/bike-id) conj #{} (:offheap records))))
