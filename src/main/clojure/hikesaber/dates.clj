(ns hikesaber.dates
  (:require [clojure.core.memoize :as memo]
            [clj-time.core :as t]
            [clj-time.coerce :as tc]
            [clj-time.predicates :as tp]
            [clj-time.format :as tf])
  (:import [java.util Date]
           [org.joda.time DateTime]))

(def month-year-formatter (tf/formatter "M/d/yyyy"))
(def time-formatter (tf/formatter "M/d/yyyy H:m"))
(def legacy-time-formatter (tf/formatter "yyyy-M-d H:m"))

(def day-formatter (tf/formatter "d"))
(def month-formatter (tf/formatter "MM"))
(def year-formatter (tf/formatter "yyyy"))

(def memo-parse (memo/lru (fn [s] (tf/parse time-formatter s))
                          :lru/threshold 10))

(def memo-parse-month-year (memo/lru (fn [s] (tf/parse month-year-formatter s))
                                     :lru/threshold 10))

(def to-month-string (memo/lru (fn [d] (tf/unparse month-formatter d))
                               :lru/threshold 10))

(def to-year-string (memo/lru (fn [d] (tf/unparse year-formatter d))
                              :lru/threshold 10))

(def to-day-string (memo/lru (fn [d] (tf/unparse day-formatter d))
                             :lru/threshold 10))
(let [hour-strings (vec (concat (map #(str "0" %1) (range 10)) (map str (range 10 24))))
      minute-strings  (vec (concat (map #(str "0" %1) (range 10)) (map str (range 10 60))))]
  (def to-hour-minute-string (memo/lru (fn [minutes-of-day]
                                         (let [hour (quot minutes-of-day 60)
                                               minute (rem minutes-of-day 60)]
                                           (str (nth hour-strings hour)
                                                ":"
                                                (nth minute-strings minute))))
                                       :lru/threshold 10)))

(defn from-2014-time-format [timestr]
  (->> timestr
       (tf/parse time-formatter)))

(defn from-2013-time-format [timestr]
  (->> timestr
       (tf/parse legacy-time-formatter)))

(defn to-date-time [inst]
  (tc/from-date inst))

(defn minute-bucket [num-minutes ^DateTime date-time]
  (let [hour (.getHourOfDay date-time)
        minutes  (* num-minutes (quot (.getMinuteOfHour date-time) num-minutes))]
    (+ (* 60 hour) minutes)))

(defn start-of-day [^DateTime date-time]
  (.withTimeAtStartOfDay date-time))

(defn from-millis [^long millis]
  (Date. millis))

(defn weekday? [datetime]
  (tp/weekday? datetime))

(defn to-datetime [timestr]
  (memo-parse timestr))

(defn month-year [^DateTime datetime]
  (.withTimeAtStartOfDay (.withDayOfMonth datetime 1)))


(defn day-month-year [datetime]
  {:day (to-day-string datetime)
   :month (to-month-string datetime)
   :year (to-year-string datetime)})
