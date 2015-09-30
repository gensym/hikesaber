(ns hikesaber.ride-records.helpers
  (:require [clj-time.predicates :as tp]
            [hikesaber.ride-records.ranges :as ranges]
            [hikesaber.dates :as dates])
  (:import [org.joda.time DateTime]))


(defn start-time [record] (DateTime. (:starttime record)))
(defn stop-time [record] (DateTime. (:stoptime record)))


(defn trimmed-records [records start-date end-date]
  "date format is M/d/yyyy"
  (if (= 0 (count records))
    records
    (let [start (if (nil? start-date)
                  (:starttime (nth records 0))
                  (.getMillis (dates/memo-parse-month-year start-date)))
          end (if (nil? end-date)
                (:stoptime (nth records (dec (count records))))
                (.getMillis (.plusDays (dates/memo-parse-month-year end-date) 1)))]
      (if (< start end)
        (ranges/trim-to-range records start end)
        (ranges/trim-to-range records end end)))))
