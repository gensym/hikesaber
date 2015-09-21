(ns hikesaber.analysis.usage-by-time
  (:require [hikesaber.ride-records.helpers :as h]
            [hikesaber.calculations.binned-frequencies :as b]
            [hikesaber.calculations.frequencies :as freq]
            [hikesaber.dates :as d]))

(comment (def recs (hikesaber.record-cache/load-cached-records)))

(defn- make-day-filter [{:keys [include-weekend include-weekdays]}]
  (cond
   (and include-weekend include-weekdays) (constantly true)
   include-weekend (complement d/weekday?)
   include-weekdays d/weekday?
   :else (constantly false)))

(defn weekday-usage-by-time-of-day
  ([records] (weekday-usage-by-time-of-day records {}))
  ([records opts]
     (let [options (merge {:include-weekend true
                           :include-weekdays true} opts)
           binned (transduce (comp (map h/start-time)
                                   (filter (make-day-filter options)))
                             (completing (fn [binner value] (b/add binner value)))
                             (b/create (partial d/minute-bucket 15)
                                       (partial d/start-of-day))
                             records)]
       (map (fn [[bin count-by-date]]
              {:time (d/to-hour-minute-string bin)
               :percentiles (freq/percentiles [0.05 0.25 0.50 0.75 0.95] (vals count-by-date))})
            (:bins binned)))))

