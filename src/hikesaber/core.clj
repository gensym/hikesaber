(ns hikesaber.core
  (:require [clojure.string :as string])
  (:import [java.io BufferedReader InputStreamReader]
          [java.util.zip ZipFile ZipEntry ZipInputStream]))

(defn foo
  "I don't do a whole lot."
  [x]
  (println x "Hello, World!"))

(defn to-maps [csv-lines]
  (map #(string/split % #",") csv-lines))


(defn divvy-data-lines []
  (let [zipfile  (ZipFile.
                  "/Users/daltenburg/data/divvy/Divvy_Stations_Trips_2014-Q1Q2.zip")
        entries (.entries zipfile)
        entry (.nextElement entries)]
    (line-seq (BufferedReader. (InputStreamReader. (.getInputStream zipfile entry))))))
