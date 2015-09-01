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
            [hikesaber.analysis.usage-by-time :as usage]))

(defn load-records []
  (cache/load-cached-records))


(def rides-by-time-of-day
  (memo/lru
   (fn [loaded-records weekend?]
     (divvy/count-by-time-of-day loaded-records 15 (not weekend?)))
   :lru/threshold 10))

(def usage-by-time-of-day
  (fn [loaded-records]
    (pres/usage-by-time-json
     (usage/weekday-usage-by-time-of-day loaded-records))))

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
   (comp/GET "/usage_by_time_of_day.json" []
             (json-response (usage-by-time-of-day loaded-records)))
   (comment
     (comp/GET "/time-of-day-counts.json" {{weekend? :weekend} :params}
               (json/write-str
                (json-response
                 (rides-by-time-of-day loaded-records (read-string (or weekend? "false"))))))
     (comp/GET "/monthly-counts.json" req (json-response (json/write-str (rides-by-month loaded-records)))))
   (comp/GET "/ping" req {:status 200 :headers {"Content-Type" "text/html"} :body "hello"})
   (route/not-found "<p>Page not found.</p>")))

(defn start [loaded-records]
  (hs/run-server (handler/site (make-routes loaded-records)) {:port 8080}))
