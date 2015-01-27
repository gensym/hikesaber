(defproject hikesaber "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [org.clojure/data.json "0.2.5"]
                 [clj-time "0.8.0"]
                 [org.clojure/core.memoize "0.5.6"]
                 [org.clojure/tools.logging "0.3.1"]
                 [org.slf4j/slf4j-log4j12 "1.7.1"]
                 [log4j/log4j "1.2.17" :exclusions [javax.mail/mail
                                                    javax.jms/jms
                                                    com.sun.jmdk/jmxtools
                                                    com.sun.jmx/jmxri]]
                 [http-kit "2.1.16"]
                 [ring "1.2.1"]
                 [compojure "1.1.9"]
                 [camel-snake-kebab "0.2.4"]]

    :main ^:skip-aot hikesaber.core
    :target-path "target/%s"
    :profiles {:uberjar {:aot :all}})
