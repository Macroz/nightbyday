(ns nightbyday.main
  (:require [enfocus.core :as ef]
            [crate.core :as crate]
            )
  (:require-macros [enfocus.macros :as em])
  (:use-macros [crate.def-macros :only [defpartial]]))

(defn log [& messages]
  (when js/console
    (.log js/console (apply str messages))))

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
(def paper (atom nil))

(defn init-day1 []
  (let [scene {:background {:image "img/village.png" :size [1920 1080]}
               :objects [{:position [650 600]
                          :image "img/body1.png"
                          :size [389 277]
                          :scale 0.2}
                         {:position [1000 620]
                          :image "img/person1.png"
                          :size [202 444]
                          :flip true
                          :scale 0.2}
                         {:position [1130 460]
                          :image "img/person1.png"
                          :size [202 444]
                          :flip true
                          :scale 0.15}
                         {:position [500 880]
                          :image "img/person1.png"
                          :size [202 444]
                          :scale 0.3}]}
        ]
    (swap! data
           (fn [data]
             (assoc data :scene scene)))))

(defn draw-object [object]
  (let [[x y] (get object :position [100 100])
        [w h] (get object :size [50 50])
        scale (get object :scale 1.0)
        flip (get object :flip false)
        [w h] [(* w scale) (* h scale)]
        image (get object :image "img/default.png")
        object (.image @paper image x y w h)
        cx (+ x (/ w 2))
        by (+ y h)
        text (.text @paper cx by (str x ", " y))]
    (doto text
      (.attr "stroke" "#fff")
      (.attr "fill" "#fff")
      (.attr "font-size" "20"))
    (when flip
      (.transform object "s-1,1"))
    (doto object
      ;;(.click (fn [_] (.remove object)))
      (.drag (fn [dx dy x y event]
               (let [cx (+ x (/ w 2))
                     by (+ y h)]
                 (.attr object "x" x)
                 (.attr object "y" y)
                 (.attr text "x" cx)
                 (.attr text "y" by)
                 (.attr text "text" (str x ", " y))))))))

(defn refresh-scene []
  (let [{image :image [width height] :size} (get-in @data [:scene :background])
        background (.image @paper image 0 0 width height)
        _ (log "foo")
        objects (get-in @data [:scene :objects])
        objects (doall (map draw-object objects))
        _ (log "bar")
        ]
    ))

(defn startup []
  (let [raphael (.-Raphael js/window)
        new-paper (raphael 0 0 1920 1080)]
    (swap! paper (fn [_] new-paper))
    (init-day1)
    (refresh-scene)))

;;(0f0 startup []
;;  (goog.Timer/callOnce game-loop 100))
