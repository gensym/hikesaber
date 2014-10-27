(ns hikesaber.webserver
  (:require [org.httpkit.server :as hs]
            [compojure.core :as comp]
            [compojure.route :as route]
            [compojure.handler :as handler]
            [clojure.data.json :as json]
            [hikesaber.divvy-ride-statistics :as divvy]))

(def loaded-records (hikesaber.divvy-ride-records/load-from-file))

(def rides-by-time-of-day
  (memoize
   (fn [loaded-records weekend?]
     (map (fn [[time count]] {:time time :count count :weekend? weekend?})
          (divvy/count-by-time-of-day loaded-records 15 (not weekend?))))))

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
   (comp/GET "/weekday-rides.json" {{weekend? :weekend} :params}
             (json-response
              (rides-by-time-of-day loaded-records (read-string (or weekend? "false")))))
   (comp/GET "/ping" req {:status 200 :headers {"Content-Type" "text/html"} :body "hello"})
   (route/not-found "<p>Page not found.</p>")))

(defn start [loaded-records]
  (hs/run-server (handler/site (make-routes loaded-records)) {:port 8080}))
