(ns hikesaber.off-heap-ride-records
  (:require  [camel-snake-kebab.core :refer :all]
             [hikesaber.dates :as dates]
             [clojure.string :as string]
             [hikesaber.divvy-ride-records :as records]
             [hikesaber.util.integer-ids :as ids]
             [hikesaber.performance-tools :as perf])
  (:import [java.io BufferedReader InputStreamReader File]
           [java.util.zip ZipFile ZipEntry ZipInputStream]
           [sun.misc Unsafe]))


(def trip-id-offset 0)
(def from-station-id-offset 8)
(def to-station-id-offset 12)
(def bike-id-offset 16)
(def start-time-offset 20)
(def stop-time-offset 28)
(def user-type-offset 36)
(def object-size 38)

(defn get-trip-id [unsafe object-offset]
  (.getLong unsafe (+ object-offset trip-id-offset)))

(defn set-trip-id! [unsafe object-offset trip-id]
  (.putLong unsafe (+ object-offset trip-id-offset) trip-id))

(defn get-from-station-id [unsafe object-offset]
  (.getInt unsafe (+ object-offset from-station-id-offset)))

(defn set-from-station-id! [unsafe object-offset station-id]
  (.putInt unsafe (+ object-offset from-station-id-offset) station-id))

(defn get-to-station-id [unsafe object-offset]
  (.getInt unsafe (+ object-offset to-station-id-offset)))

(defn set-to-station-id! [unsafe object-offset station-id]
  (.putInt unsafe (+ object-offset to-station-id-offset) station-id))

(defn get-bike-id [unsafe object-offset]
  (.getInt unsafe (+ object-offset bike-id-offset)))

(defn set-bike-id! [unsafe object-offset bike-id]
  (.putInt unsafe (+ object-offset bike-id-offset) bike-id))

(defn get-start-time [unsafe object-offset]
  (.getLong unsafe (+ object-offset start-time-offset)))

(defn set-start-time! [unsafe object-offset start-time]
  (.putLong unsafe (+ object-offset start-time-offset) start-time))

(defn get-stop-time [unsafe object-offset]
  (.getLong unsafe (+ object-offset stop-time-offset)))

(defn set-stop-time! [unsafe object-offset stop-time]
  (.putLong unsafe (+ object-offset stop-time-offset) stop-time))

(defn get-user-type [unsafe object-offset]
  (.getChar unsafe (+ object-offset user-type-offset)))

(defn set-user-type! [unsafe object-offset user-type]
  (.putChar unsafe (+ object-offset user-type-offset) user-type))

(defn- getUnsafe []
  (let [f (.getDeclaredField Unsafe "theUnsafe")]
    (.setAccessible f true)
    (.get f nil)))

(def read-int (comp int read-string))
(def read-long read-string)
(def user-type->id {"Customer" \C
                    "Member" \M})

(def id->user-type {\C "Customer"
                    \M "Member"})

(defn make-record-collection [loaded-records]
  (let [unsafe (getUnsafe)
        num-records (count loaded-records)
        required-size  (* object-size num-records)
        address (.allocateMemory unsafe required-size)]

    (try
      (loop [records loaded-records
             offset address]
        (if (empty? records)

          {:address address
           :count num-records
           :unsafe unsafe
           :destroy (fn [] (.freeMemory unsafe address))}

          (let [record (first records)]
            (set-trip-id! unsafe offset (read-long (:trip-id record)))
            (set-from-station-id! unsafe offset (read-int (:from-station-id record)))
            (set-to-station-id! unsafe offset (read-int (:to-station-id record)))
            (set-bike-id! unsafe offset (read-int (:bikeid record)))
            (set-start-time! unsafe offset (.getMillis (:starttime record)))
            (set-stop-time! unsafe offset (.getMillis (:stoptime record)))
            (set-user-type! unsafe offset (user-type->id (:usertype record)))

            (recur (next records)
                   (+ offset object-size)))))

      (catch Throwable t
        (do
          (.freeMemory unsafe address)
          (throw t)))
      )))

(defn destroy! [record-collection]
  ((:destroy record-collection)))


(defn read-record [record-collection index]
  (let [address (+ (:address record-collection) (* index object-size))
        unsafe (:unsafe record-collection)]
    {
     :trip-id (str (get-trip-id unsafe address))
     :from-station-id (str (get-from-station-id unsafe address))
     :to-station-id (str (get-to-station-id unsafe address))
     :bikeid (str (get-bike-id unsafe address))
     :startime (dates/from-millis (get-start-time unsafe address))
     :stoptime (dates/from-millis (get-stop-time unsafe address))
     :usertype (id->user-type (get-user-type unsafe address))
     }))
