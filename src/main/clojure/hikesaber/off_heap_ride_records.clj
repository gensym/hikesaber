(ns hikesaber.off-heap-ride-records
  (:require
   [hikesaber.dates :as dates]
   [clojure.string :as string]
   [hikesaber.divvy-ride-records :as records]
   [hikesaber.util.integer-ids :as ids]
   [hikesaber.performance-tools :as perf])
  (:import [sun.misc Unsafe]
           [org.joda.time DateTime]))

;; This namespace is an implementation of the ideas in http://mechanical-sympathy.blogspot.com/2012/10/compact-off-heap-structurestuples-in.html


(def trip-id-offset 0)
(def from-station-id-offset 8)
(def to-station-id-offset 12)
(def bike-id-offset 16)
(def start-time-offset 20)
(def stop-time-offset 28)
(def user-type-offset 36)
(def object-size 38)

(defn get-trip-id [^Unsafe unsafe object-offset]
  (.getLong unsafe (+ object-offset trip-id-offset)))

(defn set-trip-id! [^Unsafe unsafe object-offset trip-id]
  (.putLong unsafe (+ object-offset trip-id-offset) trip-id))

(defn get-from-station-id [^Unsafe unsafe object-offset]
  (.getInt unsafe (+ object-offset from-station-id-offset)))

(defn set-from-station-id! [^Unsafe unsafe object-offset station-id]
  (.putInt unsafe (+ object-offset from-station-id-offset) station-id))

(defn get-to-station-id [^Unsafe unsafe object-offset]
  (.getInt unsafe (+ object-offset to-station-id-offset)))

(defn set-to-station-id! [^Unsafe unsafe object-offset station-id]
  (.putInt unsafe (+ object-offset to-station-id-offset) station-id))

(defn get-bike-id
  ([^Unsafe unsafe object-offset]
     (.getInt unsafe (+ object-offset bike-id-offset)))
  ([record]
     (let [^Unsafe unsafe (:unsafe record)]
       (.getInt unsafe (+ (:address record) bike-id-offset)))))

(defn set-bike-id! [^Unsafe unsafe object-offset  bike-id]
  (.putInt unsafe (+ object-offset bike-id-offset) bike-id))

(defn get-start-time [^Unsafe unsafe object-offset]
  (.getLong unsafe (+ object-offset start-time-offset)))

(defn set-start-time! [^Unsafe unsafe object-offset start-time]
  (.putLong unsafe (+ object-offset start-time-offset) start-time))

(defn get-stop-time [^Unsafe unsafe object-offset]
  (.getLong unsafe (+ object-offset stop-time-offset)))

(defn set-stop-time! [^Unsafe unsafe object-offset stop-time]
  (.putLong unsafe (+ object-offset stop-time-offset) stop-time))

(defn get-user-type [^Unsafe unsafe object-offset]
  (.getChar unsafe (+ object-offset user-type-offset)))

(defn set-user-type! [^Unsafe unsafe object-offset user-type]
  (.putChar unsafe (+ object-offset user-type-offset) user-type))

(defn- getUnsafe ^Unsafe []
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

(defn starttime ^DateTime [loaded-record]
  (:starttime loaded-record))

(defn stoptime ^DateTime [loaded-record]
  (:stoptime loaded-record))

(defprotocol Disposable (dispose [this]))

(defprotocol AddressableUnsafe
  (unsafe [this])
  (address [this]))

(defprotocol RecordObject
  (bike-id [this]))

(defn make-record-object [unsafe offset]
  (reify

    RecordObject
    (bike-id [_] (get-bike-id unsafe offset))

    clojure.lang.ILookup

    (valAt [this key not-found]
      (case key
        :bikeid (get-bike-id unsafe offset) 
        :starttime (get-start-time unsafe offset)
        :stoptime (get-stop-time unsafe offset)
        not-found))
    
    (valAt [this key] (.valAt this key nil))))

(defn unsafe-reduce
  ([^Unsafe unsafe address num-records f]
     (if (= 0 num-records)
       (f)
       (loop [i 1
              offset address
              ret (f (make-record-object unsafe offset))]
         (if (= i num-records)
           ret
           (let [offset (+ offset object-size)
                 ret (f ret (make-record-object unsafe offset))]
             (if (reduced? ret)
               @ret
               (recur (inc i) offset ret)))))))
  ([unsafe address num-records f v]
     (loop [i 0
            offset address
            ret v]
       (if (= i num-records)
         ret
         (let [ret (f ret (make-record-object unsafe offset))]
           (if (reduced? ret)
             @ret
             (recur (inc i) (+ offset object-size) ret)))))))

;; deftype - implement Indexed,Counted,
(deftype RecordCollection [^Unsafe unsafe address num-records]

  clojure.core.protocols/CollReduce
  (coll-reduce [_ f] (unsafe-reduce unsafe address num-records f))
  (coll-reduce [_ f v] (unsafe-reduce unsafe address num-records f v))

  clojure.lang.Indexed
  (nth [_ i] (make-record-object unsafe (+ address (* i object-size)) ))

  (count [_] num-records)

  AddressableUnsafe
  (unsafe [_] unsafe)
  (address [_] address)

  Disposable

  (dispose [_] (.freeMemory unsafe address)))

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
          (RecordCollection. unsafe address num-records)
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

(defn read-record [record-collection index]
  (let [address (+ (:address record-collection) (* index object-size))
        unsafe (:unsafe record-collection)]
    {
     :trip-id (str (get-trip-id unsafe address))
     :from-station-id (str (get-from-station-id unsafe address))
     :to-station-id (str (get-to-station-id unsafe address))
     :bikeid (str (get-bike-id unsafe address))
     :starttime (dates/from-millis (get-start-time unsafe address))
     :stoptime (dates/from-millis (get-stop-time unsafe address))
     :usertype (id->user-type (get-user-type unsafe address))
     }))


