(ns hikesaber.analysis.station-geometry
  (:require [hikesaber.ride-records.helpers :as h]
            [criterium.core :as crit]))

(comment
  (def recs (hikesaber.record-cache/load-cached-records)))

(defn nil-safe-max [& args]
  (let [non-nil-args (filter (complement nil?) args)]
    (if (> (count non-nil-args) 0)
      (apply max non-nil-args)
      nil)))

(defn nil-safe-min [& args]
  (let [non-nil-args (filter (complement nil?) args)]
    (if (> (count non-nil-args) 0)
      (apply min non-nil-args)
      nil)))

(defn make-record-filter [options]
  (if-let [station-id (:station-id options)]
    (fn [rec] ((into #{} [(:from-station-id rec)
                          (:to-station-id rec)])
               station-id))
    (constantly true)))

(defn merge-station-history [history station-info]
  (if (nil? history)
    {:id (:id station-info)
     :names #{(:name station-info)}}
    (assoc history :names (conj (:names history)
                                (:name station-info)))))

(defn stations [ride-records]
  (loop [idx 0
         m (transient {})]
    (if (= idx (count ride-records))
      (persistent! m)
      (let [v (nth ride-records idx)]
        (recur (inc idx)
               (let [station-info (h/to-station-info v)]
                 (assoc! m (:id station-info)
                         (merge-station-history (get m (:id station-info))
                                                station-info))))))))

(defn rectangular-area
  "Available options - :station-id"
  ([ride-records]
     (rectangular-area ride-records {}))
  ([ride-records opts]
     (let [pred (make-record-filter opts)
           station-snapshots  (loop [idx 0
                                     m (transient #{})]
                                (if (= idx (count ride-records))
                                  (persistent! m)
                                  (let [v (nth ride-records idx)]
                                    (if (pred v)
                                      (recur (inc idx)
                                             (conj!
                                              (conj! m (h/to-station-info v))
                                              (h/from-station-info v)))
                                      (recur (inc idx) m)))))]
       (reduce (fn [m v]
                 (assoc m
                         :max-lon (nil-safe-max (:max-lon m) (:longitude v))
                         :min-lon (nil-safe-min (:min-lon m) (:longitude v))
                         :max-lat (nil-safe-max (:max-lat m) (:latitude v))
                         :min-lat (nil-safe-min (:min-lat m) (:latitude v))))
               {} station-snapshots))))
