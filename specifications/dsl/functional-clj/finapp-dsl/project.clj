(defproject finapp-dsl "0.1.0-SNAPSHOT"
  :description "Functional DSL for financial applications"
  :url "http://example.com/financial-dsl"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [[org.clojure/clojure "1.11.1"]
                 [org.clojure/clojurescript "1.11.60"]]
  :plugins [[lein-cljsbuild "1.1.8"]
            [lein-figwheel "0.5.20"]]
  :source-paths ["src"]
  :cljsbuild {:builds
              [{:id "dev"
                :source-paths ["src"]
                :figwheel true
                :compiler {:main finapp-dsl.core
                           :asset-path "js/out"
                           :output-to "resources/public/js/main.js"
                           :output-dir "resources/public/js/out"
                           :target :nodejs
                           :optimizations :none
                           :source-map true
                           :source-map-timestamp true}}
               {:id "prod"
                :source-paths ["src"]
                :compiler {:output-to "target/js/main.js"
                           :output-dir "target/js/out"
                           :optimizations :advanced
                           :target :nodejs}}]}
  :figwheel {:server-port 3449
             :repl true}
  :profiles {:dev {:dependencies [[org.clojure/tools.namespace "1.3.0"]]
                   :source-paths ["dev"]}
             :uberjar {:aot :all
                       :jvm-opts ["-Dclojure.compiler.direct-linking=true"]}}
  :main ^:skip-aot finapp-dsl.core
  :target-path "target/%s")
