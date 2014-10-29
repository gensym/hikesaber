(ns hikesaber.divvy-ride-statistics
  (:require [hikesaber.divvy-ride-records :as records]))

(def loaded-records (records/load-from-files))

(defn count-by [loaded-records f]
  (->> loaded-records
       (group-by f)
       (map (fn [[k v]] [k (count v)]))
       (sort-by first)))

(defn ->>to-> [f]
  (fn [& args]
    (let [c (count args)]
      (apply f (concat [(last args)] (take (dec c) args))))))

(def filter> (->>to-> filter))

(defn count-by-time-of-day [loaded-records num-minutes weekday?]
  (-> loaded-records
      (filter> #(= weekday? (records/weekday? %)))
      (count-by (partial records/to-minute-interval-label num-minutes))))

(defn count-by-absolute-month [loaded-records]
  (-> loaded-records
      (count-by records/month-with-year)))
