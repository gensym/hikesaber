(defproject hikesaber "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.7.0-RC1"]
                 [org.clojure/data.json "0.2.5"]
                 [clj-time "0.8.0"]
                 [org.clojure/core.memoize "0.5.6"]
                 [org.clojure/tools.logging "0.3.1"]
                 [org.slf4j/slf4j-log4j12 "1.7.1"]
                 [log4j/log4j "1.2.17" :exclusions [javax.mail/mail
                                                    javax.jms/jms
                                                    com.sun.jmdk/jmxtools
                                                    com.sun.jmx/jmxri]]
                 [criterium "0.4.3"]
                 [http-kit "2.1.16"]
                 [ring "1.2.1"]
                 [compojure "1.1.9"]
                 [org.openjdk.jmh/jmh-core "1.9.3"]
                 [org.openjdk.jmh/jmh-generator-annprocess "1.9.3"]]

  :source-paths ["src/main/clojure"]
  :java-source-paths ["src/main/java"]
  :test-paths ["src/test/clojure"]
  :profiles { :benchmark {:main org.openjdk.jmh.Main
                          :java-source-paths ["src/main/java" "src/benchmark/java"]
                          :prep-tasks [["compile" "hikesaber.benchmark.harness"] "javac" "compile"]}
             :build-cache { :jvm-opts ["-Xmx8g"] }
             :uberjar {:aot :all}}
  :aliases {
            "make-record-cache"
            ["with-profile" "build-cache" "trampoline" "run" "-m" "hikesaber.record-cache/make-cache"]

            "clean-record-cache"
            ["trampoline" "run" "-m" "hikesaber.record-cache/clean-cache"]}

  :main ^:skip-aot hikesaber.core
  :target-path "target/%s"
  :jvm-opts ["-DinternStrings=true" "-XX:StringTableSize=1000003"])
