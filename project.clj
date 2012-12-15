(defproject nightbyday "0.1.0-SNAPSHOT"
  :description "Night By Day, Ludum Dare #25 game programming competition entry by @zorcam"
  :url "http://nightbyday.heroku.com"
  :dependencies [[org.clojure/clojure "1.4.0"]
                 [ring/ring-jetty-adapter "1.1.0"]
                 [crate "0.2.1"]
                 [enfocus "1.0.0-beta2"]
                 [compojure "1.1.1"]
                 [hiccup "1.0.1"]
                 [ring "1.1.6"]
                 [cheshire "3.0.0"]
                 ]
  :plugins [[lein-cljsbuild "0.2.7"]]
  :cljsbuild {
              :builds [{
                        :source-path "src"
                        :compiler {
                                   :output-to "resources/public/js/cljs.js"
                                   :optimizations :whitespace
                                   :pretty-print true}}]})
