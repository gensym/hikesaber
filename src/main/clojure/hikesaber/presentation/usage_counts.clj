(ns hikesaber.presentation.usage-counts
  (:require [clojure.data.json :as json]
            [hikesaber.dates :as d]))

(defn date-range [date-range-data]
  (json/write-str (select-keys date-range-data [:start :end])
                  :value-fn (fn [key value]
                              (d/millis->datetime-string value))))

(defn usage-by-time-json [usage-data]
  (json/write-str (sort-by :time usage-data)
                  :key-fn (fn [key]
                            (if (float? key)
                              (clojure.pprint/cl-format nil "~,2F" key)
                              (name key)))))
