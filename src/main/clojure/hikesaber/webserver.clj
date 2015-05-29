(ns hikesaber.webserver
  (:require [clojure.core.memoize :as memo]
            [org.httpkit.server :as hs]
            [ring.util.response :as resp]
            [compojure.core :as comp]
            [compojure.route :as route]
            [compojure.handler :as handler]
            [clojure.data.json :as json]
            [hikesaber.divvy-ride-statistics :as divvy]))

(def loaded-records divvy/loaded-records)

(def rides-by-time-of-day
  (memo/lru
   (fn [loaded-records weekend?]
     (divvy/count-by-time-of-day loaded-records 15 (not weekend?)))
   :lru/threshold 10))

(def rides-by-month
  (memo/lru
   (fn [loaded-records]
     (divvy/count-by-absolute-month loaded-records))
   :lru/threshold 10))

(defn string-response [data]
  {:status 200
   :headers {"Content-Type" "text/html"}
   :body data})

(defn json-response [data]
  {:status 200
   :headers {"Content-Type" "application/json"}
   :body (json/write-str data)})

(defn make-routes [loaded-records]
  (comp/routes
   (route/resources "/")
   (comp/GET "/" [] (resp/resource-response "index.html" {:root "public"}))
   (comp/GET "/time-of-day-counts.json" {{weekend? :weekend} :params}
             (json-response
              (rides-by-time-of-day loaded-records (read-string (or weekend? "false")))))
   (comp/GET "/monthly-counts.json" req (json-response (rides-by-month loaded-records)))
   (comp/GET "/ping" req {:status 200 :headers {"Content-Type" "text/html"} :body "hello"})
   (route/not-found "<p>Page not found.</p>")))

(defn start [loaded-records]
  (hs/run-server (handler/site (make-routes loaded-records)) {:port 8080}))
