(defproject ecos "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :jvm-opts ^:replace ["-Xmx1g" "-server"]
  :plugins [[lein-cljsbuild "1.0.5"]
            [lein-figwheel "0.3.1" :exclusions [orc.clojure/core.cache]]]
  :dependencies [;;[ankha "0.1.5.1-21e6ac"]
                 [cheshire "5.2.0"]
                 [cider/cider-nrepl "0.8.2"]
                 [clj-http "1.0.0"]
                 [clj-time "0.6.0"]
                 [com.cognitect/transit-clj "0.8.259"]
                 [com.cognitect/transit-cljs "0.8.202"]
                 [com.datomic/datomic-free "0.9.5153"]
                 ;;[com.datomic/datomic-pro "0.9.5067" :exclusions [org.slf4j/slf4j-nop]]
                 [com.firebase/firebase-token-generator "1.0.1"]
                 [compojure "1.1.8"]
                 [datascript "0.11.1"]
                 [figwheel "0.3.1"]
                 [hiccup "1.0.5"]
                 [http-kit "2.1.18"]
                 [javax.servlet/servlet-api "2.5"]
                 [log4j "1.2.16"]
                 [log4j/apache-log4j-extras "1.1"]
                 [org.clojure/clojure "1.7.0-beta2"]
                 [org.clojure/clojurescript "0.0-3255" :classifier "aot" :exclusions [org.clojure/tools.reader org.clojure/data.json]]
                 [org.clojure/tools.reader "0.9.2" :classifier "aot"]
                 [org.clojure/data.json "0.2.6" :classifier "aot"]
                 ;;[org.clojure/core.async "0.1.346.0-17112a-alpha"]
                 [org.clojure/google-closure-library "0.0-20150505-021ed5b3"]
                 [org.clojure/google-closure-library-third-party "0.0-20150505-021ed5b3"]
                 [org.clojure/tools.logging "0.2.6"]
                 ;;[org.clojure/tools.nrepl "0.2.3"]
                 [org.omcljs/om "0.8.8"]
                 [org.slf4j/slf4j-api "1.6.2"]
                 [org.slf4j/slf4j-log4j12 "1.6.2"]
                 [ring/ring "1.2.2"]
                 [ring/ring-core "1.2.0"]
                 [ring/ring-devel "1.2.0"]
                 [ring/ring-jetty-adapter "1.2.0"]
                 [ring/ring-json "0.2.0"]
                 [slingshot "0.10.3"]
                 [sonian/carica "1.0.3" :exclusions [org.clojure/tools.logging]]]
  :source-paths ["src"
                 "yaks/taika/src"]
  :profiles {:dev {:plugins []}}
  :clean-targets ^{:protect false} [:target-path "resources/public/js/bin-debug"]
  :figwheel {:css-dirs ["resources/public/css"]}
  :cljsbuild {:builds [{:id           "dev"
                        :source-paths ["src/cljs"]
                        :figwheel     true
                        :compiler     {:asset-path    "/js/bin-debug"
                                       :main          ecos.client.core
                                       :output-to     "resources/public/js/bin-debug/main.js"
                                       :output-dir    "resources/public/js/bin-debug/"
                                       :optimizations :none
                                       :pretty-print  true
                                       :preamble      ["react/react.js"]
                                       :externs       ["react/externs/react.js"]
                                       :source-map    true}}
                       {:id "whitespace"
                        :source-paths ["src/cljs"]
                        :compiler {:output-to "resources/public/cljs/whitespace/frontend.js"
                                   :output-dir "resources/public/cljs/whitespace"
                                   :optimizations :whitespace
                                   ;; :source-map "resources/public/cljs/whitespace/sourcemap.js"
                                   }}

                       {:id "test"
                        :source-paths ["src/cljs" "test/cljs"]
                        :compiler {:pretty-print true
                                   :output-to "resources/public/cljs/test/frontend-dev.js"
                                   :output-dir "resources/public/cljs/test"
                                   :optimizations :advanced
                                   :externs ["datascript/externs.js"
                                             "test-js/externs.js"
                                             "src-cljs/js/react-externs.js"
                                             "src-cljs/js/pusher-externs.js"
                                             "src-cljs/js/ci-externs.js"
                                             "src-cljs/js/analytics-externs.js"
                                             "src-cljs/js/intercom-jquery-externs.js"]
                                   :source-map "resources/public/cljs/test/sourcemap-dev.js"}}
                       {:id "production"
                        :source-paths ["src/cljs"]
                        :compiler {:pretty-print false
                                   :output-to "resources/public/cljs/production/frontend.js"
                                   :output-dir "resources/public/cljs/production"
                                   :optimizations :advanced
                                   :externs ["datascript/externs.js"
                                             "react/externs/react.js"
                                             "src-cljs/js/react-externs.js"
                                             "src-cljs/js/pusher-externs.js"
                                             "src-cljs/js/ci-externs.js"
                                             "src-cljs/js/analytics-externs.js"
                                             "src-cljs/js/intercom-jquery-externs.js"]
                                   ;; :source-map "resources/public/cljs/production/sourcemap-frontend.js"
                                   }}]
              :test-commands {"frontend-unit-tests"
                              ["node_modules/karma/bin/karma" "start" "karma.conf.js" "--single-run"]}})
