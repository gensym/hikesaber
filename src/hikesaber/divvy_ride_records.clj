(ns hikesaber.divvy-ride-records
  (:require  [camel-snake-kebab.core :refer :all]
             [hikesaber.dates :as dates]
             [clojure.string :as string]
             [hikesaber.performance-tools :as perf])
  (:import [java.io BufferedReader InputStreamReader]
           [java.util.zip ZipFile ZipEntry ZipInputStream]))
(def filename "/Users/daltenburg/data/divvy/Divvy_Stations_Trips_2014-Q1Q2.zip")
(def filenames ["/Users/daltenburg/data/divvy/Divvy_Stations_Trips_2014-Q1Q2.zip"
                "/Users/daltenburg/data/divvy/Divvy_Stations_Trips_2013.zip"])

(def data-files
  {"/Users/daltenburg/data/divvy/Divvy_Stations_Trips_2014-Q1Q2.zip"
   {:stoptime (fn [v] [:stoptime (dates/from-2014-time-format v)])
    :starttime (fn [v] [:starttime (dates/from-2014-time-format v)])}
   "/Users/daltenburg/data/divvy/Divvy_Stations_Trips_2013.zip"
   {:stoptime (fn [v] [:stoptime (dates/from-2013-time-format v)])
    :starttime (fn [v] [:starttime (dates/from-2013-time-format v)])
    :birthday (fn [v] [:birthyear v])}})

;; These are properties that are known to have a finite set of values.
;; Be specifiying them, we can potentially decrease the memory footprint
;; of the dataset by interning their values.
(def repeated-string-properties-masseuses
  (reduce (fn [m v] (assoc m v (fn [x] [v (perf/string x)]))) {}
          #{:from-station-name
            :to-station-name
            :bikeid
            :from-station-id
            :to-station-id
            :usertype
            :gender
            :birthyear
            :tripduration}))

(defn massage-record [masseuse record]
  "Masseuse is a hashmap of keys to functions. Each function takes a single argument. For each key in record that has a function in masseuse, replace the the key-value pair in the record with that returned from call the function on the original value in the record."
  (reduce
   (fn [m [k v]]
     (let [[k-1 v-1] ((get masseuse k (fn [_] [k v])) v)]
       (assoc (dissoc m k) k-1 v-1)))
   {}
   record))

(defn weekday? [record]
  (dates/weekday? (:starttime record)))

(defn month-with-year [record]
  (dates/month-year (:starttime record)))

(defn day-with-month [record]
  (dates/day-month-year (:starttime record)))

(defn annotate-with [key f record]
  (assoc record key (f record)))

(defn to-minute-interval
  ([interval-length]
     (let [num-slices (int (/ 60 interval-length))
           all-minute-labels (map (partial format "%02d") (range 60))
           minute-labels (map #(nth all-minute-labels (* interval-length %)) (range num-slices))]
       (fn [record]
         (let [dt (:starttime record)
               increments (/
                           (+ (.getMinuteOfHour dt)
                              (* 60 (.getHourOfDay dt)))
                           interval-length)]
           (str (format "%02d" (int (/ increments num-slices)))
                ":"
                (nth minute-labels (mod increments num-slices)))))))
  ([interval-length record]
     ((to-minute-interval interval-length) record)))

(defn to-minute-interval-label [interval-length record]
  (let [num-slices (int (/ 60 interval-length))
        all-minute-labels (map (partial format "%02d") (range 60))
        minute-labels (map #(nth all-minute-labels (* interval-length %)) (range num-slices))
        dt  (:starttime record)
        increments (/
                    (+ (.getMinuteOfHour dt)
                       (* 60 (.getHourOfDay dt)))
                    interval-length)]
    (str (format "%02d" (int (/ increments num-slices)))
         ":"
         (nth minute-labels (mod increments num-slices)))))


(defn- to-map-seq [s]
  (let [keys (map (comp keyword ->kebab-case) (string/split (first s) #","))]
    (map (fn [line] (zipmap keys (string/split line #",")))
         (rest s))))

(defn from-file [filename]
  (let [zipfile  (ZipFile. filename)
        entries (enumeration-seq (.entries zipfile))
        entry  (first (filter #(re-find #"Divvy_Trips.*\.csv$" (.getName %)) entries))]
    (line-seq (BufferedReader. (InputStreamReader. (.getInputStream zipfile entry))))))

(defn from-files [filenames]
  (mapcat )
  (let [zipfile  (ZipFile. filename)
        entries (.entries zipfile)
        entry (.nextElement entries)]
    (line-seq (BufferedReader. (InputStreamReader. (.getInputStream zipfile entry)))))  )

(defn load-from-file []
  (to-map-seq (from-file filename)))

(defn load-from-files []
  (mapcat (fn [[filename data-mappings]]
            (->> filename
                 (from-file)
                 (to-map-seq)
                 (map #(massage-record (merge
                                        repeated-string-properties-masseuses
                                        data-mappings) %))))
          data-files))

;; Create static definition so that toher namespaces don't need to reload them with every compile
(def loaded (load-from-files))
