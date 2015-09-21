(ns hikesaber.ride-records.ranges-tests
  (:require [clojure.test :refer :all]
            [hikesaber.ride-records.off-heap-ride-records :as recs]
            [hikesaber.ride-records.ranges :as r])
  (:import [org.joda.time DateTime]))

(defn- make-record
  ([year month day]
     (let [start (-> (DateTime. year month day 00 00))
           end (-> (DateTime. year month day 00 00))]
       {:trip-id "1"
        :from-station-id "2"
        :to-station-id "3"
        :bikeid "4"
        :starttime start
        :stoptime end
        :usertype "Customer"})))

(defn- make-records [hour minute start-day num-days num-record-per-day]
  (mapcat (fn [offset]
            (let [day (+ start-day offset)]
              (map (fn [_] (make-record day hour minute)) (range num-record-per-day))))
          (range num-days)))


(deftest date-range-tests
  (testing "empty should return empty"
    (let [records (recs/make-record-collection [])]
      (is (= {} (r/date-range records)))))

  (testing "a single record should have its date as start and end of the range"
    (let [record (make-record 2014 01 01)
          records (recs/make-record-collection [record])]
      (is (= {:start (.getMillis (:starttime record))
              :end (.getMillis (:stoptime record))}
             (r/date-range records)))))

  (testing "a collection of records should get start from the first and end from the last"
    (let [first-record (make-record 2014 01 01)
          middle-record (make-record 2015 01 01)
          last-record (make-record 2015 06 21)
          records (recs/make-record-collection [first-record
                                                middle-record
                                                last-record])]
      (is (= {:start (.getMillis (:starttime first-record))
              :end (.getMillis (:stoptime last-record))}
             (r/date-range records))))))

(deftest records-in-range-tests
     (testing "empty should return empty"
       (let [start (.getMillis (DateTime. 2014 01 01 00 00))
             end (.getMillis (DateTime. 2015 01 01 00 00))
             records (recs/make-record-collection [])]
         (is (= 0 (count (r/trim-to-range records start end))))))

     (testing "a single element should be returned"
       (let [start (.getMillis (DateTime. 2014 01 01 00 00))
             end (.getMillis (DateTime. 2014 01 01 00 00))
             record (make-record 2014 01 01)
             records (recs/make-record-collection [record])
             trimmed (r/trim-to-range records start end)]
         (is (= 1 (count trimmed)))
         (is (= start (:starttime (nth trimmed 0))))))

     (testing "should trim early and late elements"
       (let [start (.getMillis (DateTime. 2014 01 01 00 00))
             end (.getMillis (DateTime. 2015 01 01 00 00))
             early (make-record 2013 06 01)
             late (make-record 2015 06 01)
             record (make-record 2014 2 01)
             records (recs/make-record-collection [early record late])
             trimmed (r/trim-to-range records start end)]
         (is (= 1 (count (r/trim-to-range records start end))))
         (is (=  (.getMillis (DateTime. 2014 02 01 00 00)) (:starttime (nth trimmed 0))))))

     (testing "should return all elements"
       (let [start (.getMillis (DateTime. 2013 01 01 00 00))
             end (.getMillis (DateTime. 2015 06 01 00 00))
             early (make-record 2013 06 01)
             late (make-record 2015 06 01)
             record (make-record 2014 2 01)
             records (recs/make-record-collection [early record late])
             trimmed (r/trim-to-range records start end)]
         (is (= 3 (count (r/trim-to-range records start end))))))

     (testing "should be empty when endtime predates all"
       (let [start (.getMillis (DateTime. 2013 01 01 00 00))
             end (.getMillis (DateTime. 2013 02 01 00 00))
             record (make-record 2014 2 01)
             records (recs/make-record-collection [record])
             trimmed (r/trim-to-range records start end)]
         (is (= 0 (count (r/trim-to-range records start end))))))

     (testing "should be empty when starttime postdates all"
       (let [start (.getMillis (DateTime. 2015 01 01 00 00))
             end (.getMillis (DateTime. 2015 02 01 00 00))
             record (make-record 2014 2 01)
             records (recs/make-record-collection [record])
             trimmed (r/trim-to-range records start end)]
         (is (= 0 (count (r/trim-to-range records start end)))))))
