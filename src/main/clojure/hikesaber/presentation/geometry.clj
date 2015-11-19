(ns hikesaber.presentation.geometry
  (:require [clojure.data.json :as json]))

(defn rectangular-area [area]
  (json/write-str (select-keys area [:max-lon :min-lon :max-lat :min-lat])))


(defn stations [station-list]
  (json/write-str
   (sort-by :id (map #(select-keys % [:id :names]))) station-list))
