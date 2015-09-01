(ns hikesaber.presentation.usage-counts
  (:require [clojure.data.json :as json]))

(defn usage-by-time-json [usage-data]
  (json/write-str (sort-by :time usage-data)
                  :key-fn (fn [key]
                            (if (float? key)
                              (clojure.pprint/cl-format nil "~,2F" key)
                              (name key)))))
