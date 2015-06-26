(ns hikesaber.off-heap-ride-records
  (:require
   [hikesaber.dates :as dates]
            [clojure.string :as string]
            [hikesaber.divvy-ride-records :as records]
            [hikesaber.util.integer-ids :as ids]
            [hikesaber.performance-tools :as perf])
  (:import [java.nio ByteBuffer]
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

(defn get-trip-id [^ByteBuffer unsafe object-offset]
  (.getLong unsafe (+ object-offset trip-id-offset)))

(defn set-trip-id! [^ByteBuffer unsafe object-offset trip-id]
  (.putLong unsafe (+ object-offset trip-id-offset) trip-id))

(defn get-from-station-id [^ByteBuffer unsafe object-offset]
  (.getInt unsafe (+ object-offset from-station-id-offset)))

(defn set-from-station-id! [^ByteBuffer unsafe object-offset station-id]
  (.putInt unsafe (+ object-offset from-station-id-offset) station-id))

(defn get-to-station-id [^ByteBuffer unsafe object-offset]
  (.getInt unsafe (+ object-offset to-station-id-offset)))

(defn set-to-station-id! [^ByteBuffer unsafe object-offset station-id]
  (.putInt unsafe (+ object-offset to-station-id-offset) station-id))

(defn get-bike-id
  ([^ByteBuffer unsafe object-offset]
     (.getInt unsafe (+ object-offset bike-id-offset)))
  ([record]
     (let [^ByteBuffer unsafe (:unsafe record)]
       (.getInt unsafe (+ (:address record) bike-id-offset)))))

(defn set-bike-id! [^ByteBuffer unsafe object-offset  bike-id]
  (.putInt unsafe (+ object-offset bike-id-offset) bike-id))

(defn get-start-time [^ByteBuffer unsafe object-offset]
  (.getLong unsafe (+ object-offset start-time-offset)))

(defn set-start-time! [^ByteBuffer unsafe object-offset start-time]
  (.putLong unsafe (+ object-offset start-time-offset) start-time))

(defn get-stop-time [^ByteBuffer unsafe object-offset]
  (.getLong unsafe (+ object-offset stop-time-offset)))

(defn set-stop-time! [^ByteBuffer unsafe object-offset stop-time]
  (.putLong unsafe (+ object-offset stop-time-offset) stop-time))

(defn get-user-type [^ByteBuffer unsafe object-offset]
  (.getChar unsafe (+ object-offset user-type-offset)))

(defn set-user-type! [^ByteBuffer unsafe object-offset user-type]
  (.putChar unsafe (+ object-offset user-type-offset) user-type))

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
  (reify RecordObject (bike-id [_] (get-bike-id unsafe offset))))

(defn unsafe-reduce
  ([^ByteBuffer unsafe num-records f]
     (if (= 0 num-records)
       (f)
       (loop [i 1
              offset 0
              ret (f (reify RecordObject (bike-id [_] (get-bike-id unsafe offset))))]
         (if (= i num-records)
           ret
           (let [offset (+ offset object-size)
                 ret (f ret (reify RecordObject (bike-id [_] (get-bike-id unsafe offset))))]
             (if (reduced? ret)
               @ret
               (recur (inc i) offset ret)))))))
  ([unsafe num-records f v]
     (loop [i 0
            offset 0
            ret v]
       (if (= i num-records)
         ret
         (let [ret (f ret (reify RecordObject (bike-id [_] (get-bike-id unsafe offset))))]
           (if (reduced? ret)
             @ret
             (recur (inc i) (+ offset object-size) ret)))))))

;; deftype - implement Indexed,Counted,
(deftype RecordCollection [^ByteBuffer unsafe num-records]

  clojure.core.protocols/CollReduce
  (coll-reduce [_ f] (unsafe-reduce unsafe num-records f))
  (coll-reduce [_ f v] (unsafe-reduce unsafe num-records f v))

  clojure.lang.Indexed
  (nth [_ i] (make-record-object unsafe (* i object-size) ))

  (count [_] num-records)

  AddressableUnsafe
  (unsafe [_] unsafe)

  Disposable

  (dispose [_] ))

(defn make-record-collection [loaded-records]
  (let [ num-records (count loaded-records)
        required-size  (* object-size num-records)
        unsafe (ByteBuffer/allocateDirect required-size)]
    (loop [idx 0
           records loaded-records
           offset 0]
      (if (empty? records)
        (RecordCollection. unsafe num-records)
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
                 (long (+ offset object-size))))))))

(defn read-record [record-collection index]
  (let [address  (* index object-size)
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


