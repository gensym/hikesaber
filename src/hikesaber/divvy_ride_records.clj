(ns hikesaber.divvy-ride-records
  (:require  [camel-snake-kebab.core :refer :all]
             [clj-time.core :as t]
             [clj-time.predicates :as tp]
             [clj-time.format :as tf]
             [clojure.string :as string])
  (:import [java.io BufferedReader InputStreamReader]
          [java.util.zip ZipFile ZipEntry ZipInputStream]))
(def filename "/Users/daltenburg/data/divvy/Divvy_Stations_Trips_2014-Q1Q2.zip")

(def time-formatter (tf/formatter "M/d/yyyy H:m"))

(defn weekday? [record]
  (->> record
       (:starttime)
       (tf/parse time-formatter)
       (tp/weekday?)))


(defn to-minute-interval-label [interval-length record]
  (let [num-slices (int (/ 60 interval-length))
        all-minute-labels (map (partial format "%02d") (range 60))
        minute-labels (map #(nth all-minute-labels (* interval-length %)) (range num-slices))
        dt (tf/parse time-formatter (:starttime record))
        increments (/
                    (+ (.getMinuteOfHour dt)
                       (* 60 (.getHourOfDay dt)))
                    interval-length)]
    (str (int (/ increments num-slices))
         ":"
         (nth minute-labels (mod increments num-slices)))))



(defn- to-map-seq [s]
  (let [keys (map (comp keyword ->kebab-case) (string/split (first s) #","))]
    (map (fn [line] (zipmap keys (string/split line #",")))
         (rest s))))

(defn from-file [filename]
  (let [zipfile  (ZipFile. filename)
        entries (.entries zipfile)
        entry (.nextElement entries)]
    (line-seq (BufferedReader. (InputStreamReader. (.getInputStream zipfile entry)))))  )
