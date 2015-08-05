(ns hikesaber.calculations.frequencies-tests
  (:require [clojure.test :refer :all]
            [hikesaber.calculations.frequencies :as freq]))

(deftest percentiles
  (testing "Should split simple numbers into percentiles"
    (let [expected {0.1 9
                    0.3 29
                    0.5 49
                    0.8 79
                    0.9 89}
          actual (freq/percentiles [0.1 0.3 0.5 0.8 0.9] (range 100))]
      (is (= expected actual))))

  (testing "Should work with small collections"
    (let [exected {0.1 1
                   0.3 1
                   0.5 1
                   0.51 2
                   0.8 2
                   0.9 2
                   1.0 2}
          actual (freq/percentiles [0.1 0.3 0.5 0.51 0.8 0.9 1.0] [1 2])]
      (is (= exected actual))))

  (testing "Should work with a single-elemenet collection"
    (let [expected {0.1 42
                   0.5 42
                   0.9 42}]
      (is (= expected (freq/percentiles [0.1 0.5 0.9] [42])))))
  (testing "Should work with an empty collection"
    (let [expected {0.1 0
                    0.5 0
                    0.9 0}]
      (is (= expected (freq/percentiles [0.1 0.5 0.9] []))))))
