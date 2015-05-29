(ns hikesaber.util.integer-ids-test
  (:require
   [clojure.test :refer :all]
   [hikesaber.util.integer-ids :as ids]))

(deftest should-return-stored-value
  (let [idset (ids/make-idset)]
    (is (= (ids/produce-id! idset "foo") (ids/produce-id! idset "foo")))))

(deftest should-produce-unique-values
  (let [idset (ids/make-idset)]
    (is (not (= (ids/produce-id! idset "foo") (ids/produce-id! idset "bar"))))))

(deftest should-get-value
  (let [idset (ids/make-idset)
        id (ids/produce-id! idset "foo")]
    (is (= "foo" (ids/find-value idset id)))))
