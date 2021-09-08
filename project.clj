(defproject calculator-api "0.0.1-SNAPSHOT"
  :description "Calculator RESTful API, written in clojure, using Antlr4 parser generator."
  :url "https://github.com/pouyacode/calculator-api"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.10.1"]
                 [io.pedestal/pedestal.service "0.5.9"]

                 ;; Remove this line and uncomment one of the next lines to
                 ;; use Immutant or Tomcat instead of Jetty:
                 [io.pedestal/pedestal.jetty "0.5.9"]
                 ;; [io.pedestal/pedestal.immutant "0.5.9"]
                 ;; [io.pedestal/pedestal.tomcat "0.5.9"]

                 [ch.qos.logback/logback-classic "1.2.3" :exclusions [org.slf4j/slf4j-api]]
                 [org.slf4j/jul-to-slf4j "1.7.26"]
                 [org.slf4j/jcl-over-slf4j "1.7.26"]
                 [org.slf4j/log4j-over-slf4j "1.7.26"]
                 [org.clojure/data.json "2.4.0"]
                 [hiccup "1.0.5"]
                 [org.antlr/antlr4 "4.9.2"]]
  :min-lein-version "2.0.0"
  :resource-paths ["config", "resources", "docs"]
  ;; If you use HTTP/2 or ALPN, use the java-agent to pull in the correct alpn-boot dependency
  ;:java-agents [[org.mortbay.jetty.alpn/jetty-alpn-agent "2.0.5"]]
  :profiles {:dev {:aliases {"run-dev" ["trampoline" "run" "-m" "calculator-api.server/run-dev"]}
                   :dependencies [[io.pedestal/pedestal.service-tools "0.5.9"]]
                   :plugins [[lein-shell "0.5.0"]]}
             :uberjar {:aot [calculator-api.server]}}
  :plugins [[lein-marginalia "0.9.1"]]
  :main ^{:skip-aot true} calculator-api.server
  :java-source-paths ["src/java"]
  :aliases
  {"sass" ["shell"
           "sass" "src/sass/style.sass:resources/public/vendor/css/style.min.css"
           "--style=compressed"]
   "sass-watch" ["shell"
                 "sass" "src/sass/style.sass:resources/public/vendor/css/style.min.css"
                 "--style=compressed"
                 "--watch"]
   "front-end" ["shell"
               "npm" "run" "release"]})
