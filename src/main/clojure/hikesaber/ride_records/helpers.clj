(ns hikesaber.ride-records.helpers
  (:require [clj-time.predicates :as tp])
  (:import [org.joda.time DateTime]))


(defn start-time [record] (DateTime. (:starttime record)))
