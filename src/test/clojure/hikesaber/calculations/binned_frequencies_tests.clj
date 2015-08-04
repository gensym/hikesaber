(ns hikesaber.calculations.binned-frequencies-tests
  (:require [clojure.test :refer :all]
            [hikesaber.calculations.binned-frequencies :as b]))

(defn- integer-binner [i]
  (b/create (fn [x] (mod x i))
            (fn [x] (int (/ x i)))))

(deftest should-create-empty-binner []
  (let [bin (integer-binner 10)]
    (is (= {} (b/bins bin)))))

(deftest should-add-to-empty-binner []
  (let [b1 (integer-binner 10)
        b2 (b/add b1 45)]
    (is (= {5 [1]} (b/bins b2)))))

(deftest should-increment-existing-group []
  (let [bins (-> (integer-binner 10)
                 (b/add 45)
                 (b/add 45))]
    (is (= {5 [2]} (b/bins bins)))))

(deftest combining-empty-binners-should-be-empty []
  (let [e (integer-binner 10)]
    (is (= e (b/+ e e)))))

(deftest bins-should-return-sorted-bins []
  (let [bin (-> (integer-binner 10)
              (b/add 45)
              (b/add 55)
              (b/add 55))]
    (is (= {5 [1 2]} (b/bins bin)))))

(deftest combining-binners-should-include-all-groups []
  (let [root (integer-binner 10)
        b1 (b/add root 45)
        b2 (b/add root 35)]
    (is (= {5 [1, 1]} (b/bins (b/+ b1 b2))))))

