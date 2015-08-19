(ns hikesaber.analysis.usage-by-time-tests
  (:require [clojure.test :refer :all]
            [hikesaber.ride-records.off-heap-ride-records :as r]
            [hikesaber.analysis.usage-by-time :as u])
  (:import [org.joda.time DateTime]))

(defn- make-record [day hour minute]
  (let [start (-> (DateTime. 2014 01 01 00 00)
                  (.plusDays day)
                  (.plusHours hour)
                  (.plusMinutes minute))
        end (.plusHours start 1)]
    {:trip-id "1"
     :from-station-id "2"
     :to-station-id "3"
     :bikeid "4"
     :starttime start
     :stoptime end
     :usertype "Customer"}))

(defn- make-records [hour minute start-day num-days num-record-per-day]
  (mapcat (fn [offset]
            (let [day (+ start-day offset)]
              (map (fn [_] (make-record day hour minute)) (range num-record-per-day))))
          (range num-days)))

(comment   (testing "should return usage by time as percentiles"
    (let [expected  [{:time "01:30",
                      :percentiles {0.05 12
                                    0.25 15
                                    0.50 16
                                    0.75 19
                                    0.95 20}}
                     {:time "01:45"
                      :percentiles {0.05 0
                                    0.25 7
                                    0.50 20
                                    0.75 21
                                    0.95 21}}]

          records (concat (make-records 1 30 0 5 12)
                          (make-records 1 30 5 20 15)
                          (make-records 1 30 25 25 16)
                          (make-records 1 30 50 25 19)
                          (make-records 1 30 75 20 20)

                          (make-records 1 45 5 20 7)
                          (make-records 1 45 25 25 20)
                          (make-records 1 45 50 25 21)
                          (make-records 1 45 75 20 21))

          records (r/make-record-collection records)
          actual (u/weekday-usage-by-time-of-day records)]
      (is (= expected actual)))))

(deftest usage-by-time-test
  (testing "empty should return empty"
    (let [records (r/make-record-collection [])]
      (is (= [] (u/weekday-usage-by-time-of-day records))))))

