(ns nightbyday.server
  (:use compojure.core)
  (:require [compojure.route :as route])
  (:use [ring.adapter.jetty :only [run-jetty]])
  (:use [hiccup core element page]))

(defn welcome-page []
  (html5 [:html
          [:head
           [:title "Night By Day"]]
          [:body
           [:h1 "Night By Day"]
           [:a {:href "/game"} "Play"]]]))

(defn game-page []
  (html5 [:html
          [:head
           [:title "Night By Day"]
           (include-js "js/raphael-min.js")
           (include-js "js/cljs.js")]
          [:body {:onload "nightbyday.main.startup();"}
           [:canvas#canvas]]]))

(defroutes handler
  (GET "/" [] (welcome-page))
  (GET "/game" [] (game-page))
  (route/resources "/")
  (route/not-found "Page not found!"))

(def app (-> handler))

(def server (atom nil))

(defn- run []
  (when @server
    (.stop @server))
  (swap! server (fn [old]
                  (run-jetty #'app {:port 8080 :join? false}))))

(defn -main [port]
  (run-jetty #'app {:port (Integer. port)}))
