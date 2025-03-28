(defproject finapp-dsl "0.1.0-SNAPSHOT"
  :description "Functional DSL for financial applications"
  :url "http://example.com/financial-dsl"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [[org.clojure/clojure "1.11.1"]
                 [org.clojure/clojurescript "1.11.60"]]
  :plugins [[lein-cljsbuild "1.1.8"]
            [lein-figwheel "0.5.20"]
            [lein-codox "0.10.8"]
            [lein-marginalia "0.9.1"]
            [lein-cljfmt "0.8.0"]
            [lein-auto "0.1.3"]]
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
  :profiles {:dev {:dependencies [[org.clojure/tools.namespace "1.3.0"]
                                 [com.nextjournal/clerk "0.14.919"]
                                 [metasoarous/oz "2.0.0-alpha5"]
                                 [cljdoc/cljdoc-analyzer "1.0.2"]
                                 [org.slf4j/slf4j-nop "1.7.36"]]
                   :source-paths ["dev"]
                   :resource-paths ["doc"]
                   :plugins [[lein-auto "0.1.3"]]
                   :auto {:default {:file-pattern #"\.(clj|cljs|cljc)$"
                                    :paths ["src" "test"]
                                    :hook lein-codox}}}
             :uberjar {:aot :all
                       :jvm-opts ["-Dclojure.compiler.direct-linking=true"]}}
  :main ^:skip-aot finapp-dsl.core
  :target-path "target/%s"
  
  ;; Documentation configuration
  :codox {:output-path "doc/api"
          :source-uri "https://github.com/AppAthanor/finapp-dsl-architecture/blob/{version}/{filepath}#L{line}"
          :metadata {:doc/format :markdown}
          :themes [:default :rdash]
          :doc-files ["doc/guides/getting_started.md"
                     "doc/concepts/regions.md"
                     "doc/concepts/segments.md"
                     "doc/concepts/business_rules.md"]
          :html {:transforms [[:head] [:append [:script {:src "https://cdnjs.cloudflare.com/ajax/libs/mermaid/9.3.0/mermaid.min.js"}]]
                             [:body] [:append [:script "mermaid.initialize({startOnLoad:true});"]]]}}
  
  :aliases {"docs" ["do" ["codox"] ["marg"]]
            "all-tests" ["test"]
            "check-all" ["do" ["check"] ["eastwood"] ["kibit"]]
            "generate-docs" ["run" -m "finapp-dsl.doc-generator"]
            "watch-docs" ["auto" "codox"]
            "check-docs" ["with-profile" "dev" "run" -m "finapp-dsl.doc-checker"]
            "publish-docs" ["do" ["docs"] ["run" -m "finapp-dsl.doc-publisher"]]
            "dev-docs" ["do" ["docs"] ["run" -m "finapp-dsl.doc-server"]]
            "verify" ["do" ["test"] ["check-docs"] ["generate-docs"]]})
