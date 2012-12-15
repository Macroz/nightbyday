(ns nightbyday.main
  (:require [enfocus.core :as ef]
            [crate.core :as crate]
            )
  (:require-macros [enfocus.macros :as em])
  (:use-macros [crate.def-macros :only [defpartial]]))

(defn log [& messages]
  (.log js/console (apply str messages)))

(defn clj->js
  "Recursively transforms ClojureScript maps into Javascript objects,
   other ClojureScript colls into JavaScript arrays, and ClojureScript
   keywords into JavaScript strings."
  [x]
  (cond
   (string? x) x
   (keyword? x) (name x)
   (map? x) (.-strobj (reduce (fn [m [k v]]
                                (assoc m (clj->js k) (clj->js v))) {} x))
   (coll? x) (apply array (map clj->js x))
   :else x))

(def data (atom {}))

(defn startup []
  (let [raphael (.-Raphael js/window)
        paper (raphael 10 50 320 200)
        circle (.circle paper 50 40 10)]
    (doto circle
        (.attr "fill" "#f00")
        (.attr "stroke" "#fff")
        (.click (fn [&foo] (.attr circle "fill" "#0f0")))
        )))

;;(defn startup []
;;  (goog.Timer/callOnce game-loop 100))
