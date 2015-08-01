(ns hikesaber.off-heap-ride-records-test
  (:import [org.joda.time DateTime])
  (:require [clojure.test :refer :all]
            [hikesaber.ride-records.off-heap-ride-records :as r]))

(defn- make-record []
  {:trip-id "1"
   :from-station-id "2"
   :to-station-id "3"
   :bikeid "4"
   :starttime (DateTime. 2014 6 30 23 57)
   :stoptime (DateTime. 2014 7 01 00 23)
   :usertype "Customer"})

(deftest should-include-record-count
  (let [records (r/make-record-collection (map (fn [_] (make-record))  (range 12)))]
    (is (= 12 (count records)))))

(deftest should-get-nth
  (let [records (r/make-record-collection (map (fn [x] (assoc (make-record) :bikeid (str x)) )  (range 12)))]
    (is (= 3 (r/bike-id (nth records 3 ))))))

(deftest should-transduce
  (let [records (r/make-record-collection (map (fn [x] (assoc (make-record) :bikeid (str x)) )  (range 12)))]
    (is (= (into #{} (range 12)) (transduce (map r/bike-id) conj #{} records)))))

(deftest should-get-by-keyword
  (let [records (r/make-record-collection [(assoc (make-record) :bikeid "123")])]
    (is (= 123 (:bikeid (nth records 0))))))

(deftest should-get-starttime-by-keyword
  (let [records (r/make-record-collection [(assoc (make-record) :starttime (DateTime. 2014 6 30 23 57))])]
    (is (= 1404190620000 (:starttime (nth records 0))))))

(deftest should-get-stoptime-by-keyword
    (let [records (r/make-record-collection [(assoc (make-record) :stoptime (DateTime. 2014 6 30 23 57))])]
    (is (= 1404190620000 (:stoptime (nth records 0)))))  )

(deftest should-get-default-by-keyword
  (let [records (r/make-record-collection [(assoc (make-record) :bikeid "123")])]
    (is (= "foo" (get (nth records 0) :notfound "foo")))))
