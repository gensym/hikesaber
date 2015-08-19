(ns hikesaber.dates-tests
  (:require [clojure.test :refer :all]
            [hikesaber.dates :as d]))


(deftest minute-bucket-test
  (testing "15 minutes - 9:39 pm"
    (is (= (d/to-date-time #inst "2013-08-16T21:30:00.000-00:00"))
        (d/minute-bucket 15  (d/to-date-time #inst "2013-08-16T21:39:01.000-00:00"))))
  (testing "15 minutes - 4:39 am"
    (is (= (d/to-date-time #inst "2013-08-16T04:30:00.000-00:00"))
        (d/minute-bucket 15  (d/to-date-time #inst "2013-08-16T21:39:01.000-00:00"))))
  (testing "15 minutes - 4:44 am"
    (is (= (d/to-date-time #inst "2013-08-16T04:30:00.000-00:00"))
        (d/minute-bucket 15  (d/to-date-time #inst "2013-08-16T21:44:01.000-00:00"))))
  (testing "15 minutes - 4:30 am"
    (is (= (d/to-date-time #inst "2013-08-16T04:30:00.000-00:00"))
        (d/minute-bucket 15  (d/to-date-time #inst "2013-08-16T21:30:00.000-00:00"))))
  (testing "20 minutes - 4:30 am"
    (is (= (d/to-date-time #inst "2015-08-16T04:20:00.000-00:00"))
        (d/minute-bucket 20  (d/to-date-time #inst "2015-08-16T21:30:00.000-00:00")))))

(deftest start-of-day-tests
  (testing "12:31 pm"
    (is (= (d/to-date-time #inst "2013-08-16T00:00:00.000-05:00"))
        (d/start-of-day (d/to-date-time #inst "2013-08-17T02:23:49.123")))))
