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
            [hikesaber.analysis.usage-by-time :as usage]
            [hikesaber.ride-records.ranges :as ranges]
            [hikesaber.ride-records.helpers :as h]))

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

   (comp/GET "/date_range.json" []
             (json-response (date-range loaded-records)))
   (comment
     (comp/GET "/monthly-counts.json" req (json-response (json/write-str (rides-by-month loaded-records)))))
   (comp/GET "/ping" req {:status 200 :headers {"Content-Type" "text/html"} :body "hello"})
   (route/not-found "<p>Page not found.</p>")))

(defn start [loaded-records]
  (hs/run-server (handler/site (make-routes loaded-records)) {:port 8080}))
