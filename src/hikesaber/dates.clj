(ns hikesaber.dates
  (:require [clojure.core.memoize :as memo]
            [clj-time.core :as t]
            [clj-time.predicates :as tp]
            [clj-time.format :as tf]))

(def time-formatter (tf/formatter "M/d/yyyy H:m"))
(def legacy-time-formatter (tf/formatter "yyyy-M-d H:m"))

(def day-formatter (tf/formatter "d"))
(def month-formatter (tf/formatter "MM"))
(def year-formatter (tf/formatter "yyyy"))

(def memo-parse (memo/lru (fn [s] (tf/parse time-formatter s))
                          :lru/threshold 10))

(def to-month-string (memo/lru (fn [d] (tf/unparse month-formatter d))
                               :lru/threshold 10))

(def to-year-string (memo/lru (fn [d] (tf/unparse year-formatter d))
                              :lru/threshold 10))

(def to-day-string (memo/lru (fn [d] (tf/unparse day-formatter d))
                             :lru/threshold 10))

(defn to-later-time-format [timestr]
  (->> timestr
       (tf/parse legacy-time-formatter)
       (tf/unparse time-formatter)))

(defn weekday? [timestr]
  (->> timestr
       (memo-parse)
       (tp/weekday?)))

(defn to-datetime [timestr]
  (memo-parse timestr))

(defn month-year [timestr]
  (let [d (memo-parse timestr)]
    {:month (to-month-string d)
     :year (to-year-string d)}))

(defn day-month-year [timestr]
  (let [d (memo-parse timestr)]
    {:day (to-day-string d)
     :month (to-month-string d)
     :year (to-year-string d)}))
