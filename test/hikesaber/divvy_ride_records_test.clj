(ns hikesaber.divvy-ride-records-test
  (:require [clojure.test :refer :all]
            [hikesaber.divvy-ride-records :as d]))

(def sample-record {:from-station-name "Lincoln Ave & Belmont Ave", :to-station-name "Broadway & Cornelia Ave", :bikeid "2006", :stoptime "7/1/2014 0:07", :trip-id "2355134", :from-station-id "131", :usertype "Subscriber", :birthyear "1988", :tripduration "604", :gender "Male", :starttime "6/30/2014 23:57", :to-station-id "303"})

(deftest should-add-weekday-annotation-from-starttime
  (let [record {:starttime "6/30/2014 23:57"}]
    (is (d/weekday? record))))
