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
;;  0.657 ±(99.9%) 0.003 s/op [Average]
;;  (min, avg, max) = (0.630, 0.657, 0.723), stdev = 0.013
;;  CI (99.9%): [0.654, 0.660] (assumes normal distribution)
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
;;  0.665 ± (99.9%) 0.005 s/op [Average]
;;  (min, avg, max) = (0.630, 0.665, 0.724), stdev = 0.020
;;  CI (99.9%): [0.660, 0.670] (assumes normal distribution)
(defn -countUniqueBikesTransduce [records]
  (count (transduce  (map :bikeid) conj #{} (:records records))))



;;Result "countUniqueBikesOffHeap":
;;  0.287 ±(99.9%) 0.006 s/op [Average]
;;  (min, avg, max) = (0.254, 0.287, 0.336), stdev = 0.026
;;  CI (99.9%): [0.281, 0.293] (assumes normal distribution)
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
;;  0.374 ±(99.9%) 0.007 s/op [Average]
;;  (min, avg, max) = (0.307, 0.374, 0.429), stdev = 0.031
;;  CI (99.9%): [0.367, 0.381] (assumes normal distribution)
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
;;  0.315 ±(99.9%) 0.007 s/op [Average]
;;  (min, avg, max) = (0.273, 0.315, 0.372), stdev = 0.029
;;  CI (99.9%): [0.308, 0.321] (assumes normal distribution)
(defn -countUniqueBikesOffHeapTransduce [records]
  (count (transduce (map ohr/bike-id) conj #{} (:offheap records))))

(defn -countUniqueBikesOffHeapTransduceKeyword [records]
  (count (transduce (map :bikeid) conj #{} (:offheap records))))
