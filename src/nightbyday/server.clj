(ns nightbyday.server
  (:use compojure.core)
  (:require [compojure.route :as route])
  (:use [ring.adapter.jetty :only [run-jetty]])
  (:use [hiccup core element page]))

(defn link-image [src]
  [:a {:href src}
   [:img {:src src
          :width 300
          :height 200}]])

(defn welcome-page []
  (html5 [:html
          [:head
           [:title "Night By Day"]
           (include-css "css/welcome.css")]
          [:body
           [:div.center
            [:div.content
             [:h1 "Night By Day"]
             [:p "A brutal murder mystery game entry to " [:a {:href "http://www.ludumdare.com"} "Ludum Dare #25"] ". Not for the faint of heart!"]
             [:p "By Markku Rontu / markku.rontu@iki.fi / @zorcam"]
             [:div.center
              [:p.play [:a {:href "/game"} "Play"]]]
             [:div.center
              [:p.screenshots (link-image "img/screenshot1.png")
               (link-image "img/screenshot2.png")]]]]]]))

(defn game-page []
  (html5 [:html
          [:head
           [:title "Night By Day"]
           (include-css "css/main.css")
           (include-js "js/raphael-min.js")
           (include-js "js/cljs.js")]
          [:body {:onload "nightbyday.main.startup();"}
           [:div.game
            [:div#paper]
            [:div.tasks.block]
            [:div.info.block]
            [:div.results.block]]
           [:div.demo]]]))

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
