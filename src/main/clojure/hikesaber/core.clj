(ns hikesaber.core
  (:gen-class)
  (:require [hikesaber.webserver :as webserver]))


(defn -main []
  (let [stop (webserver/start webserver/loaded-records)]
    (println "started")
    stop))

