(ns hikesaber.divvy-ride-records-test
  (:import [org.joda.time DateTime])
  (:require [clojure.test :refer :all]
            [hikesaber.ride-records.divvy-ride-records :as d]))

(deftest should-add-weekday-annotation-from-starttime
  (let [record {:starttime (DateTime. 2014 6 30 23 57)}]
    (is (d/weekday? record))))

(deftest should-add-minute-interval-label
  (let [record {:starttime (DateTime. 2014 6 30 23 57)}]
    (is (= "23:45" (d/to-minute-interval-label 15 record)))
    (is (= "23:50" (d/to-minute-interval-label 10 record)))
    (is (= "23:30" (d/to-minute-interval-label 30 record)))))

(deftest should-use-sortable-minute-interval-label
  (let [record  {:starttime (DateTime. 2014 6 30 0 24)}]
    (is (= "00:15" (d/to-minute-interval-label 15 record)))))

(deftest should-annotate-with
  (let [record {:key 23}]
    (is (= {:key 23
            :other 24}
           (d/annotate-with :other (comp inc :key) record)))))
