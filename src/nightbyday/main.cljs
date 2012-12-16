(ns nightbyday.main
  (:require [enfocus.core :as ef]
            [crate.core :as crate]
            [nightbyday.scenes :as scenes]
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
  (swap! data
         (fn [data]
           (assoc data :scene (scenes/day1)))))

(defpartial info-p [object]
  (if object
    [:div.info.block
     [:h1 (get object :name)]
     [:div (object :description)]]
    [:div.info]))

(defn find-task [task-id tasks]
  (when (and tasks (not (empty? tasks)))
    (let [task (first tasks)]
      (if (= task-id (task :id))
        task
        (if-let [subtask (find-task task-id (task :tasks))]
          subtask
          (recur task-id (rest tasks)))))))

(defn replace-task [old-task new-task tasks]
  (if (or (not tasks) (empty? tasks))
    []
    (loop [done-tasks []
           remaining-tasks tasks]
      (if (or (not remaining-tasks) (empty? remaining-tasks))
        done-tasks
        (let [current-task (first remaining-tasks)
              current-task (if (= current-task old-task) new-task current-task)
              current-task-tasks (replace-task old-task new-task (current-task :tasks))
              new-current-task (assoc current-task :tasks current-task-tasks)]
          (recur (conj done-tasks new-current-task) (rest remaining-tasks)))))))

(declare refresh-tasks)

(defn info [object]
  (let [selection (get @data :selection)]
    (when selection
      (doto selection
        (.attr "stroke" "")))
    (when object
      (let [[x y] (get object :position [100 100])
            [w h] (get object :size [50 50])
            scale (get object :scale 1.0)
            [sx sy] (if (vector? scale) scale [scale scale])
            [w h] [(* w sx) (* h sy)]
            selection (or selection
                          (let [rect (.rect @paper x y w h)]
                            (swap! data (fn [data]
                                          (assoc data :selection rect)))
                            rect))]
        (doto selection
          (.attr "x" x)
          (.attr "y" y)
          (.attr "width" w)
          (.attr "height" h)
          (.attr "fill" "")
          (.attr "stroke-width" "5")
          (.attr "opacity" "0.5")
          (.attr "stroke" "#333"))
        (let [tasks (get-in @data [:scene :tasks])]
          (when-let [id (object :id)]
            (let [task-id (keyword (str "examine-" (name id)))]
              (when-let [examine-task (find-task task-id tasks)]
                (let [new-task (if (examine-task :known?)
                                 (assoc examine-task :complete? true)
                                 examine-task)
                      new-tasks (replace-task examine-task new-task tasks)]
                  (swap! data (fn [data] (assoc-in data [:scene :tasks] new-tasks))))))))
        (refresh-tasks)))
    (em/at js/document
           [".info"] (em/chain
                      (em/substitute (info-p object))
                      (em/add-class "show")))))

(defn draw-object [object]
  (let [[x y] (get object :position [100 100])
        [w h] (get object :size [50 50])
        scale (get object :scale 1.0)
        [sx sy] (if (vector? scale) scale [scale scale])
        flip (get object :flip false)
        [w h] [(* w sx) (* h sy)]
        image (when-let [image (object :image)]
                (.image @paper image x y w h))
        cx (+ x (/ w 2))
        by (+ y h)
        ;;text (.text @paper cx by (str x ", " y))
        rect (when-not image (.rect @paper x y w h))
        ]
    ;; (doto text
    ;;   (.attr "stroke" "#fff")
    ;;   (.attr "fill" "#fff")
    ;;   (.attr "font-size" "20"))
    (when rect
      (doto rect
        (.attr "stroke" "#333")
        (.attr "fill" "#fff")
        (.attr "opacity" "0")
        (.click (fn [_] (info object)))))
    (when image
      (when flip
        (.transform image "s-1,1"))
      (doto image
        (.click (fn [_] (info object)))
        ;; (.drag (fn [dx dy x y event]
        ;;          (let [cx (+ x (/ w 2))
        ;;                by (+ y h)]
        ;;            (when flip
        ;;              (.transform image "s-1,1"))
        ;;            (.attr image "x" x)
        ;;            (.attr image "y" y)
        ;;            ;;(.attr text "x" cx)
        ;;            ;;(.attr text "y" by)
        ;;            (when flip
        ;;              (.transform image "s-1,1"))
        ;;            ;;(.attr text "text" (str x ", " y))
        ;;            )))
        ))
    ))

(defpartial tasks-p [tasks]
  [:ul
   (map (fn [task]
          (when (or (task :known?) (task :complete?))
            (let [name (task :name)]
              [:li (if (task :complete?)
                     {:class "complete"}
                     {})
               name
               (tasks-p (task :tasks))])))
        tasks)])

(defn refresh-tasks []
  (let [tasks (get-in @data [:scene :tasks])]
    (em/at js/document
           [".tasks"] (em/content (tasks-p tasks)))))

(defn refresh-scene []
  (let [{image :image [width height] :size} (get-in @data [:scene :background])
        background (.image @paper image 0 0 width height)
        objects (get-in @data [:scene :objects])
        objects (doall (map draw-object objects))
        text (.text @paper 100 50 "Text")]
    (doto text
      (.attr "fill" "#eee")
      (.attr "stroke" "#eee")
      (.attr "font-size" "20"))
    (doto background
      (.click (fn [e]
                (info nil)
                (let [x (.-x e)
                      y (.-y e)]
                  (doto text
                    (.attr "x" x)
                    (.attr "y" y)
                    (.attr "text" (str x ", " y))))
                )))
    (refresh-tasks)
    ))

(defn startup []
  (let [raphael (.-Raphael js/window)
        new-paper (raphael 0 0 1920 1080)]
    (swap! paper (fn [_] new-paper))
    (init-day1)
    (refresh-scene)))

;;(0f0 startup []
;;  (goog.Timer/callOnce game-loop 100))
