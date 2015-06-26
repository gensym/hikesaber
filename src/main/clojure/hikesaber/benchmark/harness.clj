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
;;  0.313 ±(99.9%) 0.004 s/op [Average]
;;  (min, avg, max) = (0.263, 0.313, 0.342), stdev = 0.017
;;  CI (99.9%): [0.309, 0.317] (assumes normal distribution)
(defn -countUniqueBikesOffHeap [records]
  (let [num-records (count (:offheap records))
        unsafe (ohr/unsafe (:offheap records))
        s ohr/object-size]
    (loop [i 0
           offset 0
           ids #{}]
      (if (= i num-records)
        (count ids)
        (recur (inc i)
               (+ offset ohr/object-size)
               (conj ids (ohr/get-bike-id unsafe offset)))))))


;;Result "countUniqueBikesOffHeapNth":
;;  0.310 ±(99.9%) 0.005 s/op [Average]
;;  (min, avg, max) = (0.278, 0.310, 0.388), stdev = 0.021
;;  CI (99.9%): [0.305, 0.315] (assumes normal distribution)
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
;;  0.346 ±(99.9%) 0.008 s/op [Average]
;;  (min, avg, max) = (0.280, 0.346, 0.404), stdev = 0.033
;;  CI (99.9%): [0.338, 0.353] (assumes normal distribution)
(defn -countUniqueBikesOffHeapTransduce [records]
  (count (transduce (map ohr/bike-id) conj #{} (:offheap records))))
