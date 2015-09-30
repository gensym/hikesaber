(ns hikesaber.analysis.station-usage
  (:require [hikesaber.ride-records.helpers :as h]
            [hikesaber.dates :as dates]))

(def recs (hikesaber.record-cache/load-cached-records))

(def trimmed (h/trimmed-records recs "06/01/2015" "06/30/2015"))

(def station-id 511)

(defn for-station [station-id record]
  (or
   (= station-id (:from-station-id record))
   (= station-id (:to-station-id record))))

(def user-type :member)

(defn for-user-type [user-type record]
  (= user-type (:user-type record)))

(def xform (comp (filter (partial for-station station-id))))

(def filtered (transduce xform conj [] trimmed))

(def members (transduce (comp (filter (partial for-station station-id))
                              (filter (partial for-user-type :member))) conj [] trimmed))

(def customers (transduce (comp (filter (partial for-station station-id))
                              (filter (partial for-user-type :customer))) conj [] trimmed))



