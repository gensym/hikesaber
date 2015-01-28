(ns hikesaber.divvy-ride-statistics
  (:require [hikesaber.divvy-ride-records :as records]
            [clojure.core.reducers :as r]))

(def loaded-records records/loaded-records)

(defn count-by [f loaded-records]
  (->> loaded-records
       (group-by f)
       (map (fn [[k v]] [k (count v)]))))

(defn count-by-iter [f loaded-records]
  (loop [lr loaded-records counts (transient {})]
    (if (empty? lr)
      (persistent! counts)
      (let [k (f (first lr))]
        (recur (rest lr)
               (assoc! counts k (inc (get counts k 0))))))))

(defn count-by-reduce [f loaded-records]
  (reduce (fn [m v]
            (let [k (f v)]
              (assoc m k (inc (get m k 0))))) {} loaded-records))

(defn count-by-reducers [f loaded-records]
  (let [produce-count (fn
                        ([] {})
                        ([m v] (let [k (f v)]
                                 (assoc m k (inc (get m k 0))))))
        merge-counts (fn
                       ([] {})
                       ([& m] (apply merge-with + m)))]
    (r/fold merge-counts produce-count loaded-records)))




(defn ->>to-> [f]
  (fn [& args]
    (let [c (count args)]
      (apply f (concat [(last args)] (take (dec c) args))))))

(def filter> (->>to-> filter))

(defn count-by-time-of-day [loaded-records num-minutes weekday?]
  (->> loaded-records
      (filter #(= weekday? (records/weekday? %)))
      (count-by (partial records/to-minute-interval-label num-minutes))
      (map (fn [[time count]] {:time time :count count :weekday? weekday?}))
      (sort-by :time)))

(defn num-days-in-month [loaded-records]
  "Get the number of days in a month that we have records for. This lets us exclude days in which no stations were operational "
  (loop [lr loaded-records months (transient {})]
    (if (empty? lr)
      (persistent! months)
      (let [day (records/day-with-month (first lr))]
        (recur (rest lr) (assoc! months day (inc (get months day 0))))))))

(defn count-by-absolute-month [loaded-records]
  (->> loaded-records
      (count-by records/month-with-year)
      (map (fn [[{month :month year :year} count]] {:month month :year year :count count}))
      (sort-by (fn [{month :month year :year}] (str year month)))))

(defn count-by-absolute-month-iter [loaded-records]
  (->> loaded-records
      (count-by-iter records/month-with-year)
      (map (fn [[{month :month year :year} count]] {:month month :year year :count count}))
      (sort-by (fn [{month :month year :year}] (str year month)))))


;; (time (count-by records/month-with-year loaded-records))
;; "Elapsed time: 30539.002509 msecs"

;; (time (count-by-iter records/month-with-year loaded-records))
;; "Elapsed time: 30590.195837 msecs"
