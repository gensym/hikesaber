(ns hikesaber.calculations.frequencies)

(defn percentiles [params coll]
  (->> params
       (map (fn [x] (dec (int (Math/ceil (* x (count coll)))))))
       (map vector params)
       (map (fn [[a b]] [a (nth coll b 0)]))
       (reduce (fn [m [a b]] (assoc m a b)) {})))
