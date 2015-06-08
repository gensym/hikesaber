(ns hikesaber.off-heap-ride-records
  (:refer-clojure :exclude [nth])
  (:require [hikesaber.dates :as dates]
            [clojure.string :as string]
            [hikesaber.divvy-ride-records :as records]
            [hikesaber.util.integer-ids :as ids]
            [hikesaber.performance-tools :as perf])
  (:import [java.io BufferedReader InputStreamReader File]
           [java.util.zip ZipFile ZipEntry ZipInputStream]
           [sun.misc Unsafe]))

;; This namespace is an implementation of the ideas in http://mechanical-sympathy.blogspot.com/2012/10/compact-off-heap-structurestuples-in.html


(def trip-id-offset 0)
(def from-station-id-offset 8)
(def to-station-id-offset 12)
(def bike-id-offset 16)
(def start-time-offset 20)
(def stop-time-offset 28)
(def user-type-offset 36)
(def object-size 38)

(defn get-trip-id [^sun.misc.Unsafe unsafe object-offset]
  (.getLong unsafe (+ object-offset trip-id-offset)))

(defn set-trip-id! [^sun.misc.Unsafe unsafe object-offset trip-id]
  (.putLong unsafe (+ object-offset trip-id-offset) trip-id))

(defn get-from-station-id [^sun.misc.Unsafe unsafe object-offset]
  (.getInt unsafe (+ object-offset from-station-id-offset)))

(defn set-from-station-id! [^sun.misc.Unsafe unsafe object-offset station-id]
  (.putInt unsafe (+ object-offset from-station-id-offset) station-id))

(defn get-to-station-id [^sun.misc.Unsafe unsafe object-offset]
  (.getInt unsafe (+ object-offset to-station-id-offset)))

(defn set-to-station-id! [^sun.misc.Unsafe unsafe object-offset station-id]
  (.putInt unsafe (+ object-offset to-station-id-offset) station-id))

(defn get-bike-id
  ([^sun.misc.Unsafe unsafe object-offset]
     (.getInt unsafe (+ object-offset bike-id-offset)))
  ([record]
     (let [^sun.misc.Unsafe unsafe (:unsafe record)]
       (.getInt unsafe (+ (:address record) bike-id-offset)))))

(defn set-bike-id! [^sun.misc.Unsafe unsafe object-offset  bike-id]
  (.putInt unsafe (+ object-offset bike-id-offset) bike-id))

(defn get-start-time [^sun.misc.Unsafe unsafe object-offset]
  (.getLong unsafe (+ object-offset start-time-offset)))

(defn set-start-time! [^sun.misc.Unsafe unsafe object-offset start-time]
  (.putLong unsafe (+ object-offset start-time-offset) start-time))

(defn get-stop-time [^sun.misc.Unsafe unsafe object-offset]
  (.getLong unsafe (+ object-offset stop-time-offset)))

(defn set-stop-time! [^sun.misc.Unsafe unsafe object-offset stop-time]
  (.putLong unsafe (+ object-offset stop-time-offset) stop-time))

(defn get-user-type [^sun.misc.Unsafe unsafe object-offset]
  (.getChar unsafe (+ object-offset user-type-offset)))

(defn set-user-type! [^sun.misc.Unsafe unsafe object-offset user-type]
  (.putChar unsafe (+ object-offset user-type-offset) user-type))

(defn- getUnsafe ^sun.misc.Unsafe []
  (let [f (.getDeclaredField Unsafe "theUnsafe")]
    (.setAccessible f true)
    (.get f nil)))

(def read-int (comp int read-string))
(def read-long read-string)
(def user-type->id {"Customer" \C
                    "Member" \M
                    "Dependent" \D})

(def id->user-type {\C "Customer"
                    \M "Member"
                    \D "Dependent"})

(defn starttime ^org.joda.time.DateTime [loaded-record]
  (:starttime loaded-record))

(defn stoptime ^org.joda.time.DateTime [loaded-record]
  (:stoptime loaded-record))

(defn make-record-collection [loaded-records]
  (let [unsafe (getUnsafe)
        num-records (count loaded-records)
        required-size  (* object-size num-records)
        address  (.allocateMemory unsafe required-size)]

    (try
      (loop [idx 0
             records loaded-records
             offset address]
        (if (empty? records)

          {:address address
           :count num-records
           :unsafe unsafe
           :destroy (fn [] (.freeMemory unsafe address))}
          (do
            (let [record (first records)]
              (try
                (set-trip-id! unsafe offset (read-long (:trip-id record)))
                (set-from-station-id! unsafe offset (read-int (:from-station-id record)))
                (set-to-station-id! unsafe offset (read-int (:to-station-id record)))
                (set-bike-id! unsafe offset (read-int (:bikeid record)))
                (set-start-time! unsafe offset (.getMillis (starttime record)))
                (set-stop-time! unsafe offset (.getMillis (stoptime record)))
                (set-user-type! unsafe offset (user-type->id (:usertype record)))
                (catch Exception e
                  (throw (RuntimeException.
                          (str "Failed parsing record " idx " (" record ")") e)))))
            (recur (inc idx)
                   (next records)
                   (long (+ offset object-size))))))

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


(defn nth [record-collection i]
  {:unsafe (:unsafe record-collection)
   :address (+ (:address record-collection) (* i object-size))})
