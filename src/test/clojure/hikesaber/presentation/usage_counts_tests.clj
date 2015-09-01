(ns hikesaber.presentation.usage-counts-tests
  (:require [clojure.test :refer :all]
            [hikesaber.presentation.usage-counts :as c]))

(defn- equal-except-whitespace [a b]
  (=
   (clojure.string/replace a #"\s" "")
   (clojure.string/replace b #"\s" "")))

(deftest usage-by-time-tests
  (testing "empty data"
    (is (= "[]" (c/usage-by-time-json []))))

  (testing "single record"
    (is (equal-except-whitespace
         (str "[{\"time\": \"00:15\","
              "\"percentiles\": {"
              "\"0.05\": 42,"
              "\"0.50\": 40,"
              "\"0.95\": 23}}]")
         (c/usage-by-time-json
          [{:time "00:15"
            :percentiles {0.05 42, 0.5 40, 0.95 23}}]))))

  (testing "sorts by time"
        (is (equal-except-whitespace
         (str "[{\"time\": \"00:15\",\"percentiles\": {\"0.05\": 1}},"
              "{\"time\": \"00:45\",\"percentiles\": {\"0.05\": 2}},"
              "{\"time\": \"01:30\",\"percentiles\": {\"0.05\": 3}}]")
         (c/usage-by-time-json
          [{:time "00:45" :percentiles {0.05 2}}
           {:time "01:30" :percentiles {0.05 3}}
           {:time "00:15" :percentiles {0.05 1}}])))))
