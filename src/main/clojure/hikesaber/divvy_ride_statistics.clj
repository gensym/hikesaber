(ns hikesaber.divvy-ride-statistics
  (:require [hikesaber.ride-records.divvy-ride-records :as records]
            [hikesaber.dates :as dates]
            [clojure.core.reducers :as r]))

(def loaded-records records/loaded)

(defn count-by [f loaded-records]
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

(defn cbtod [loaded-records num-minutes weekday?]
  (let [to-minute-interval (records/to-minute-interval num-minutes)]
    (->> loaded-records
         (filter #(= weekday? (records/weekday? %)))
         (count-by to-minute-interval))))

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
      (map (fn [[d count]] {:date d
                            :month (dates/to-month-string d)
                            :year (dates/to-year-string d)
                            :count count}))
      (sort-by :date)
      (map #(dissoc % :date))))
