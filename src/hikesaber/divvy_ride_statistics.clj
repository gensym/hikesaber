(ns hikesaber.divvy-ride-statistics
  (:require [hikesaber.divvy-ride-records :as records]))

(def loaded-records (records/load-from-files))

(defn count-by [f loaded-records]
  (->> loaded-records
       (group-by f)
       (map (fn [[k v]] [k (count v)]))))

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
