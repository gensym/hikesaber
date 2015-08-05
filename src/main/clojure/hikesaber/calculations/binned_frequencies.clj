(ns hikesaber.calculations.binned-frequencies
  (:refer-clojure :exclude [+]))

(defn- inc-in [m [k & ks]]
  "Increments an integer value within a nested associative structure. Creates assocs along the way in they don't already exist."
  (if ks
    (assoc m k (inc-in (get m k) ks))
    (assoc m k (inc (get m k 0)))))

(defn create [binning-fn grouping-fn]
  {:binning-fn binning-fn
   :grouping-fn grouping-fn
   :bins {}})

(defn add [binner value]
  (let [bin-key ((:binning-fn binner) value)
        group-key ((:grouping-fn binner) value)]
    (inc-in binner [:bins bin-key group-key])))

(defn bins [binner]
  (into {} (map (fn [[bin groups]] [bin (map (fn [[_ x]] x) (sort groups))]) (:bins binner))))

(defn + [binner-1 binner-2]
  "Combine 2 binners. Those binners must be rooted in the same create call"
  (assoc binner-1
    :bins
    (merge-with (fn [bin1 bin2]
                  (merge-with clojure.core/+ bin1 bin2))
                (:bins binner-1)
                (:bins binner-2))))
