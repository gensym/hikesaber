(ns hikesaber.webserver
  (:require [clojure.core.memoize :as memo]
            [org.httpkit.server :as hs]
            [ring.util.response :as resp]
            [compojure.core :as comp]
            [compojure.route :as route]
            [compojure.handler :as handler]
            [clojure.data.json :as json]
            [hikesaber.divvy-ride-statistics :as divvy]
            [hikesaber.record-cache :as cache]
            [hikesaber.presentation.usage-counts :as pres]
            [hikesaber.presentation.geometry :as geopres]
            [hikesaber.analysis.usage-by-time :as usage]
            [hikesaber.analysis.station-geometry :as geo]
            [hikesaber.ride-records.ranges :as ranges]
            [hikesaber.ride-records.helpers :as h]))

(defn mapbox-api-key []
  (System/getenv "MAPBOX_API_KEY"))

(defn load-records []
  (cache/load-cached-records))

(def usage-by-time-of-day
  (memo/lru
   (fn [loaded-records
        weekend?
        weekday?
        start-date
        end-date]
     (let [records (h/trimmed-records loaded-records start-date end-date)]
       (pres/usage-by-time-json
        (usage/weekday-usage-by-time-of-day records
                                            {:include-weekend weekend?
                                             :include-weekdays weekday?}))))))

(def rectangular-area
  (memo/lru
   (fn [loaded-records
        station-id
        start-date
        end-date]
     (let [records (h/trimmed-records loaded-records start-date end-date)]
       (geopres/rectangular-area
        (geo/rectangular-area records {:station-id station-id}))))))

(def stations
  (memo/lru
   (fn [loaded-records
        start-date
        end-date]
     (let [records (h/trimmed-records loaded-records start-date end-date)]
       (geopres/stations
        (geo/stations records))))))

(defn date-range [loaded-records]
  (pres/date-range (ranges/date-range loaded-records)))

(def rides-by-month
  (memo/lru
   (fn [loaded-records]
     (divvy/count-by-absolute-month loaded-records))
   :lru/threshold 10))

(defn string-response [data]
  {:status 200
   :headers {"Content-Type" "text/html"}
   :body data})

(defn json-response [json-str]
  {:status 200
   :headers {"Content-Type" "application/json"}
   :body json-str})


(defn make-routes [loaded-records]
  (comp/routes
   (route/resources "/")
   (comp/GET "/" [] (resp/content-type
                     (resp/resource-response "index.html"
                                             {:root "public"
                                              :status 200
                                              :headers {"Content-Type" "application/json"}})
                     "text/html"))
   (comp/GET "/usage_by_time_of_day.json" {{weekend? :weekend
                                            weekday? :weekday
                                            start-date :start_date
                                            end-date :end_date} :params}
             (json-response (usage-by-time-of-day loaded-records
                                                  (read-string weekend?)
                                                  (read-string weekday?)
                                                  start-date
                                                  end-date)))
   (comp/GET "/rectangular_area.json" {{start-date :start_date
                                        end-date :end_date
                                        station-id :station_id} :params}
             (json-response (rectangular-area loaded-records
                                              (read-string station-id)
                                              start-date
                                              end-date)))

   (comp/GET "/date_range.json" []
             (json-response (date-range loaded-records)))
   (comp/GET "/stations.json" {{start-date :start_date end-date :end_date} :params}
             (json-response (stations loaded-records start-date end-date)))
   (comp/GET "/mapbox_api_key" req {:status 200 :headers {"Content-Type" "text/plain"} :body (mapbox-api-key)})
   (comment
     (comp/GET "/monthly-counts.json" req (json-response (json/write-str (rides-by-month loaded-records)))))
   (comp/GET "/ping" req {:status 200 :headers {"Content-Type" "text/html"} :body "hello"})
   (route/not-found "<p>Page not found.</p>")))

(defn start [loaded-records]
  (hs/run-server (handler/site (make-routes loaded-records)) {:port 8080}))
