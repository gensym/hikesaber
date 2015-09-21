(ns hikesaber.ride-records.ranges
  (:require [hikesaber.ride-records.off-heap-ride-records :as recs]))

(defn date-range [records]
  (if (> (count records) 0)
    (let [first (nth records 0)
          last (nth records (dec (count records)))]
      {:start (:starttime first)
       :end (:stoptime last)})
    {}))

(defn- find-start-record-index [records time]
  "Find the index of the first record in records to start after (or exactly at) the given time"
  (if (= 0 (count records))
    nil
    (loop [from 0
           to  (dec (count records))]
      (println "From - " from)
      (println "To - " to)
      (let [start (:starttime (nth records from))
            end (:starttime (nth records to))]
        (cond
         (<= time start) from
         (= time end) to
         (> time end) nil
         :else
         (let [split (+ from (int (* (- to from)
                                     (/ (- time start)
                                        (- end start)))))
               splittime (:starttime (nth records split))]
           (cond
            (< time splittime) (recur from split)
            (= time splittime) split
            :else (recur (inc split) to))))))))

(defn- find-stop-record-index [records time]
  "Find the index of the first record in records to stop before (or exactly at) the given time"
  (if (= 0 (count records))
    nil
    (loop [from 0
           to  (dec (count records))]
      (println "e.From - " from)
      (println "e.To - " to)
      (let [start (:stoptime (nth records from))
            end (:stoptime (nth records to))]
        (cond
         (>= time end) to
         (= time start) from
         (< time start) nil
         :else
         (let [split (+ from (int (Math/ceil
                                   (* (- to from)
                                      (/ (- time start)
                                         (- end start))))))
               splittime (:stoptime (nth records split))]
           (cond
            (< time splittime) (recur from (dec split))
            (= time splittime) split
            :else (recur split to))))))))



(defn trim-to-range [records start-time-millis end-time-millis]
  (let [start-index (find-start-record-index records start-time-millis)
        end-index (find-stop-record-index records end-time-millis)]
    (if (or (nil? start-index) (nil? end-index))
      (recs/make-empty-collection)
      (recs/trim-to records start-index end-index))))
