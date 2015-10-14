(ns hikesaber.ride-records.ranges-tests
  (:require [clojure.test :refer :all]
            [hikesaber.ride-records.helpers :as h])
  (:import [org.joda.time DateTime]))

(deftest asof-tests
  (testing "should return first after"
    (is (= {:asof 24}
           (h/asof :asof
                   [{:asof 20}
                    {:asof 22}
                    {:asof 24}
                    {:asof 30}]
                   25))))

  (testing "should return nil when none found"
    (is (= nil
           (h/asof :asof
                   [{:asof 20}]
                   21)))))
