(ns hikesaber.ride-records.station-info
  (:require [clojure.string :as string]
            [hikesaber.dates :as dates])
  (:import [java.io BufferedReader InputStreamReader File]
           [java.util.zip ZipFile ZipEntry ZipInputStream]))


;; This shares a lot of functionality with divvy-ride-records. Shouldn't be too hard to factor out the stuff that isn't specific to the data but is just CSV munging

(def data-files
  {(clojure.java.io/resource "data/datachallenge.zip")
   [["datachallenge/Divvy_Stations_Trips_2013/Divvy_Stations_2013.csv" #inst "2014-02-07"]
    ["datachallenge/Divvy_Stations_Trips_2014/Divvy_Stations_Trips_2014_Q1Q2/Divvy_Stations_2014-Q1Q2.csv" #inst "2014-08-20"]
    ["datachallenge/Divvy_Stations_Trips_2014/Divvy_Stations_Trips_2014_Q3Q4/Divvy_Stations_2014-Q3Q4.csv" #inst "2014-12-31"]]
   (clojure.java.io/resource "data/Divvy_Trips_2015-Q1Q2.zip")
   [["Divvy_Stations_2015.csv" #inst "2015-07-01"]]})

(def default-masseuse
  {:id (fn [v] [:id (int (read-string v))])
   :latitude (fn [v] [:latitude (double (read-string v))])
   :longitude (fn [v] [:longitude (double (read-string v))])
   :dcapacity (fn [v] [:dcapacity (int (read-string v))])})

(defn from-resource [resource]
  (let [path (.getPath resource)
        zipfile  (ZipFile. (.getPath resource))
        entries (enumeration-seq (.entries zipfile))
        station-entries (filter #(re-find #"Divvy_Stations.*\.csv$" (.getName %)) entries)]
    (reduce (fn [m v] (assoc m (.getName v)
                             (line-seq (BufferedReader.
                                         (InputStreamReader. (.getInputStream zipfile v))))))
            {}
         station-entries)))

(defn snake->kebab-case [s]
  (string/replace s "_" "-"))


(defn- to-map-seq [s]
  (let [keys (map (comp keyword snake->kebab-case) (string/split (first s) #","))]
    (map (fn [line] (zipmap keys (string/split line #",")))
         (rest s))))

(defn- massage-record [masseuse record]
  "Masseuse is a hashmap of keys to functions. Each function takes a single argument. For each key in record that has a function in masseuse, replace the the key-value pair in the record with that returned from call the function on the original value in the record."
  (reduce
   (fn [m [k v]]
     (let [[k-1 v-1] ((get masseuse k (fn [_] [k v])) v)]
       (assoc (dissoc m k) k-1 v-1)))
   {}
   record))

(defn- set-as-of-date [records-for-station]
  (let [asof-dates (cons #inst "2013-06-01" (map :record-date records-for-station))]
    (map (fn [rec date] (assoc rec :asof date)) records-for-station asof-dates)))

(defn load-from-files []
  (let [station-records (mapcat (fn [[datafile internal-files-and-dates]]
                           (let [fileseqs (from-resource datafile)]
                             (mapcat (fn [[filename date]]
                                       (->> filename
                                            fileseqs
                                            to-map-seq
                                            (map #(assoc % :record-date date))
                                            (map #(massage-record default-masseuse %))))
                                     internal-files-and-dates)))
                                data-files)
        by-id (group-by :id station-records)]

    (into {} (map (fn [[id recs]] [id (set-as-of-date recs)])) by-id)))

