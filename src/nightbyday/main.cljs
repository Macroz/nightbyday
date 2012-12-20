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

(declare refresh-tasks)
(declare refresh-actions)
(declare execute-info-action!)
(declare find-task)
(declare complete-task!)
(declare find-same-object-by-id)
(declare play)
(declare init-day1!)
(declare init-night1!)
(declare refresh-scene)
(declare show-action-description!)
(declare init-outro!)

(defpartial intro-content-p []
  [:div.content
   [:h1 "Introduction"]
   [:p "It was to be your vacation. Far away from the stressful investigative work, as a detective at the Royal Police Headquarters."]
   [:p "You arrived to Maple-by-river yesterday evening. It was recommended to you by the Chief Inspector as the most peaceful place on Earth. What a shock would it become. But peace would come one way or another."]
   [:p "Staying at the Smiling Sloth Inn, you woke up in the morning to the sounds of alarmed noises outside. Deciding to skip breakfast you headed out through the main hall into the village square."]
   [:p#day1.action "Start your vacation"]])

(defpartial day1-end-content-p []
  [:div.content
   [:h1 "Night"]
   [:p "The kill did not quench your lust. It merely satisfied you for the moment. It delayed the inevitable."]
   [:p "It was time to kill again. And you wanted to start with the nosy police offier. Alex must die tonight."]
   [:p "With grim determination you leave your hideout and enter the village square."]
   [:p#night1.action "Go on with the hunt"]])

(defpartial outro-content-p []
  [:div.content
   [:h1 "Epilogue"]
   [:p "Thank you for playing the murder mystery game " [:strong "Night by Day"]]
   [:p "Made by " [:a {:href "http://markku.rontu.net"} "Markku Rontu"] " / markku.rontu@iki.fi / @zorcam"]
   [:p "Let me know what you think!"]
   [:p "The story continues soon..."]])

(defn transition-to-day1! []
  (em/at js/document
         [".demo"] (em/chain (em/fade-out 1500)
                             (em/add-class "hide")
                             (em/delay 500 (play init-day1!))
                             )))

(defn transition-to-night1! []
  (em/at js/document
         [".demo"] (em/chain (em/fade-out 1500)
                             (em/add-class "hide")
                             (em/delay 500 (play init-night1!))
                             )))

(defn init-intro! []
  (em/at js/document
         [".demo"] (em/chain (em/content (intro-content-p))
                             (em/remove-class "hide")
                             (em/fade-in 1500))
         ["#day1"] (em/listen :click transition-to-day1!)
         ))

(defn init-day1-end! []
  (execute-info-action! nil)
  (em/at js/document
         [".game"] (em/chain (em/fade-out 1500))
         [".demo"] (em/chain (em/content (day1-end-content-p))
                             (em/remove-class "hide")
                             (em/fade-in 1500))
         ["#night1"] (em/listen :click transition-to-night1!)
         ))

(defn init-outro! []
  (execute-info-action! nil)
  (em/at js/document
         [".game"] (em/chain (em/fade-out 1500))
         [".demo"] (em/chain (em/content (outro-content-p))
                             (em/remove-class "hide")
                             (em/fade-in 1500))
         ))

(defn play [scene-fn]
  (fn []
    (scene-fn)
    (em/at js/document
           [".game"] (em/fade-in 1500))))

(defn next-scene! []
  (let [current-scene-id (get-in @data [:scene :id])
        next-scene-id (get-in @data [:scene :next])]
    (log "Current " current-scene-id " next " next-scene-id)
    (case next-scene-id
      :night1 (init-day1-end!)
      :end (init-outro!))))

(defn init-day1! []
  (swap! data
         (fn [data]
           (assoc data :scene (scenes/day1))))
  (show-action-description! "People are shouting all over the square! You decide to find out what has happened." 7000)
  (em/at js/document
         [".tasks"] (em/chain (em/delay 7000 (em/fade-in 1000))))
  (refresh-scene))


(defn init-night1! []
  (swap! data
         (fn [data]
           (assoc data :scene (scenes/night1))))
  (show-action-description! "The time is right. You feel it. But you must be careful..." 7000)
  (em/at js/document
         [".tasks"] (em/chain (em/delay 7000 (em/fade-in 1000))))
  (refresh-scene))


(defn replace-object [old-object new-object objects]
  (loop [done-objects []
         remaining-objects objects]
    (if (or (not remaining-objects) (empty? remaining-objects))
      done-objects
      (let [object (first remaining-objects)]
        (if (= object old-object)
          (recur (conj done-objects new-object) (rest remaining-objects))
          (recur (conj done-objects object) (rest remaining-objects)))))))

(defn execute-talk-action! []
  (let [action (get-in @data [:actions :talk])]
    (action)))

(defn execute-examine-action! []
  (let [action (get-in @data [:actions :examine])]
    (action)))

(defn execute-generic-action! [action-id]
  (fn []
    (let [action (get-in @data [:actions action-id])]
      (action))))

(defn show-action-description! [description & timeout]
  (let [timeout (or (and timeout (first timeout)) 3000)]
    (log "Delay " timeout)
    (em/at js/document
           [".results"] (em/chain (em/content description)
                                  (em/fade-in 1000)
                                  (em/delay timeout (em/fade-out 1000))))))

(defn add-result! [result]
  (log "Add result " result)
  (swap! data (fn [data] (update-in data [:results] conj result))))

(defn result? [id]
  (contains? (@data :results) id))

(defn setup-talk-action [object]
  (let [action-info {:id :talk
                     :name (str "Talk to " (object :name))}
        talk (fn []
               (swap! data (fn [data] (update-in data [:scene :objects]
                                                 (fn [old-objects]
                                                   (let [new-object (assoc object :talked? true)
                                                         new-objects (replace-object object new-object old-objects)]
                                                     new-objects)))))
               (when (object :id)
                 (let [task-id (keyword (str "talk-" (name (object :id))))]
                   (complete-task! (find-task task-id (get-in @data [:scene :tasks])))))
               (execute-info-action! object)
               )]
    (swap! data (fn [data] (assoc-in data [:actions :talk] talk)))
    action-info))

(defn disable-action! [object action-id]
  (swap! data (fn [data]
                (update-in data [:scene :objects]
                           (fn [old-objects]
                             (let [disabled-id (keyword (str "disabled-" (name action-id) "?"))
                                   object (find-same-object-by-id object old-objects)
                                   new-object (assoc object disabled-id true)
                                   new-objects (replace-object object new-object old-objects)]
                               (log "Mark disabled " disabled-id)
                               new-objects))))))

(defn generic-action-fn [object action-id]
  (fn []
    (swap! data (fn [data] (update-in data [:scene :objects]
                                      (fn [old-objects]
                                        (let [done-id (keyword (str (name action-id) "?"))
                                              object (find-same-object-by-id object old-objects)
                                              new-object (assoc object done-id true)
                                              new-objects (replace-object object new-object old-objects)]
                                          (log "Mark done " done-id)
                                          new-objects)))))
    (when-let [results (get-in object [action-id :result])]
      (complete-task! (find-task action-id (get-in @data [:scene :tasks])))
      (doseq [result results]
        (add-result! result))
      (show-action-description! (get-in object [action-id :description])))
    (when-let [disables (get-in object [action-id :disable])]
      (doseq [disable disables]
        (disable-action! object disable)))
    (execute-info-action! object)))

(defn setup-generic-action [object action-id]
  (swap! data (fn [data] (assoc-in data [:actions action-id] (generic-action-fn object action-id))))
  {:id action-id
   :name (get-in object [action-id :name])})

(defn setup-examine-action [object]
  (let [action-info {:id :examine
                     :name (str "Examine " (object :name))}
        examine (fn []
                  (swap! data (fn [data] (update-in data [:scene :objects]
                                                    (fn [old-objects]
                                                      (let [new-object (assoc object :examined? true)
                                                            new-objects (replace-object object new-object old-objects)]
                                                        new-objects)))))
                  (when (object :id)
                    (let [task-id (keyword (str "examine-" (name (object :id))))]
                      (complete-task! (find-task task-id (get-in @data [:scene :tasks])))))
                  (execute-info-action! object)
                  )]
    (swap! data (fn [data] (assoc-in data [:actions :examine] examine)))
    action-info))

(defpartial action-p [action]
  [:div.action {:id (name (action :id))} (action :name)])

(defpartial actions-p [object]
  [:div.actions
   (if (and (not (object :examined?))
            (not (object :disabled-examine?))
            (object :examine))
     (action-p (setup-examine-action object)))
   (if (and (not (object :talked?))
            (not (object :disabled-talk?))
            (object :talk))
     (action-p (setup-talk-action object)))
   (if (and (not (object :tossrock?))
            (object :tossrock)
            (result? :knife))
     (action-p (setup-generic-action object :tossrock)))
   (if (and (not (object :stealknife?))
            (object :stealknife))
     (action-p (setup-generic-action object :stealknife)))
   (if (and (not (object :punchthroat?))
            (object :punchthroat))
     (action-p (setup-generic-action object :punchthroat)))
   (if (and (not (object :cutstomach?))
            (object :cutstomach)
            (object :punchthroat?))
     (action-p (setup-generic-action object :cutstomach)))
   (if (and (not (object :gougeeyes?))
            (object :gougeeyes)
            (object :cutstomach?))
     (action-p (setup-generic-action object :gougeeyes)))
   (if (and (not (object :tossknife?))
            (object :tossknife)
            (result? :alexdown))
     (action-p (setup-generic-action object :tossknife)))
   (if (and (not (object :hide?))
            (object :hide)
            (result? :tossedknife))
     (action-p (setup-generic-action object :hide)))
   ])

(defpartial info-p [object]
  (if object
    [:div.info.block
     [:h1 (get object :name)]
     [:div (object :description)]
     (when (object :examined?)
       [:div.examine (object :examine)])
     (when (object :talked?)
       [:div.talk "\"" (object :talk) "\""])
     (actions-p object)]
    [:div.info]))

(defn find-same-object-by-id [object objects]
  (when (and object objects)
    (let [o (first objects)]
      (if (= (object :id) (o :id))
        o
        (recur object (rest objects))))))

(defn find-object [object-id objects]
  (when (and objects (not (empty? objects)))
    (let [o (first objects)]
      (if (= object-id (o :id))
        o
        (recur object-id (rest objects))))))

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

(defn reveal-task! [task-id]
  (log "Revealing " task-id)
  (let [tasks (get-in @data [:scene :tasks])
        task (find-task task-id tasks)
        new-task (assoc task :known? true)]
    (swap! data (fn [data]
                  (update-in data [:scene :tasks]
                             (fn [tasks]
                               (replace-task task new-task tasks)))))))

(defn task-subtasks-complete? [task]
  (let [subtasks (task :tasks)]
    (or (and subtasks
             (not (empty? subtasks))
             (every? identity (map task-subtasks-complete? subtasks)))
        (task :complete?))))

(declare check-tasks-completion!)

(defn complete-task! [task]
  (when task
    (log "Complete task " (task :id))
    (swap! data (fn [data]
                  (let [new-task (assoc task :complete? true)
                        new-tasks (replace-task task new-task (get-in data [:scene :tasks]))]
                    (assoc-in data [:scene :tasks] new-tasks))))
    (when (task :reveal)
      (log "Revealing tasks " (task :reveal))
      (doall (map reveal-task! (task :reveal))))
    (check-tasks-completion!)))

(defn check-task-completion! [task]
  (when-not (task :complete?)
    (log "Checking task completion " (task :id))
    (when (and (task :known?)
               (task-subtasks-complete? task)
               (not (task :complete?)))
      (complete-task! task))
    (when (and (task :tasks) (not (empty? (task :tasks))))
      (doall (map check-task-completion! (task :tasks))))
    ))

(defn check-tasks-completion! []
  (log "Checking all tasks for completion")
  (let [tasks (get-in @data [:scene :tasks])]
    (doall (map check-task-completion! tasks))
    (when (every? :complete? tasks)
      (em/at js/document
             ["body"] (em/delay 3000 next-scene!)))))

(defn execute-info-action! [object]
  (let [object (find-same-object-by-id object (get-in @data [:scene :objects]))
        selection (get @data :selection)]
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
        (refresh-tasks)))
    (if object
      (em/at js/document
             [".info"] (em/chain (em/substitute (info-p object))
                                 (em/add-class "show")))
      (em/at js/document
             [".info"] (em/chain (em/remove-class "show"))))
    (refresh-actions)))

(defn show-object [object-id]
  (let [object (find-object object-id (get-in @data [:scene :objects]))]
    (log "Showing " object)
    (when-let [graphics (object :graphics)]
      (log "Graphics " graphics)
      (.attr graphics "opacity" "1.0"))))

(defn draw-object [object]
  (let [[x y] (get object :position [100 100])
        [w h] (get object :size [50 50])
        scale (get object :scale 1.0)
        [sx sy] (if (vector? scale) scale [scale scale])
        flip (get object :flip false)
        [w h] [(* w sx) (* h sy)]
        opacity (get object :opacity "1.0")
        image (when-let [image (object :image)]
                (.image @paper image x y w h))
        cx (+ x (/ w 2))
        by (+ y h)
        ;;text (.text @paper cx by (str x ", " y))
        rect (when-not image (.rect @paper x y w h))
        ]
    (swap! data (fn [data]
                  (let [new-object (assoc object :graphics image)
                        new-objects (replace-object object new-object (get-in data [:scene :objects]))]
                    (assoc-in data [:scene :objects] new-objects))))
    ;; (doto text
    ;;   (.attr "stroke" "#fff")
    ;;   (.attr "fill" "#fff")
    ;;   (.attr "font-size" "20"))
    (when rect
      (when (object :id)
        (set! (.-id (.-node rect)) (name (object :id))))
      (doto rect
        (.attr "stroke" "#333")
        (.attr "fill" "#fff")
        (.attr "opacity" "0")
        (.click (fn [_] (execute-info-action! object)))))
    (when image
      (when flip
        (.transform image "s-1,1"))
      (when (object :id)
        (set! (.-id (.-node image)) (name (object :id))))
      (doto image
        (.attr "opacity" opacity)
        (.click (fn [_] (execute-info-action! object)))
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

(defn alex-comes-out! [& args]
  (execute-info-action! nil)
  (show-action-description! "Alex storms out of the Police station!")
  (show-object :alexthimblewood)
  (complete-task! (find-task :lurealexout (get-in @data [:scene :tasks])))
  (refresh-tasks))

(defn refresh-actions []
  (em/at js/document
         ["#examine"] (em/listen :click execute-examine-action!)
         ["#stealknife"] (em/listen :click (execute-generic-action! :stealknife))
         ["#tossrock"] (em/listen :click (em/chain (em/delay 5000 alex-comes-out!)
                                                   (execute-generic-action! :tossrock)
                                                   ))
         ["#punchthroat"] (em/listen :click (execute-generic-action! :punchthroat))
         ["#cutstomach"] (em/listen :click (execute-generic-action! :cutstomach))
         ["#gougeeyes"] (em/listen :click (execute-generic-action! :gougeeyes))
         ["#tossknife"] (em/listen :click (execute-generic-action! :tossknife))
         ["#hide"] (em/listen :click (execute-generic-action! :hide))
         ["#talk"] (em/listen :click execute-talk-action!)))

(defn refresh-scene []
  (let [{image :image [width height] :size} (get-in @data [:scene :background])
        background (.image @paper image 0 0 width height)
        objects (get-in @data [:scene :objects])
        objects (doall (map draw-object objects))
        text (.text @paper 100 50 "")]
    (doto text
      (.attr "fill" "#eee")
      (.attr "stroke" "#eee")
      (.attr "font-size" "20"))
    (doto background
      (.click (fn [e]
                (execute-info-action! nil))))
    ;; (let [x (.-x e)
    ;;       y (.-y e)]
    ;;   (doto text
    ;;     (.attr "x" x)
    ;;     (.attr "y" y)
    ;;     (.attr "text" (str x ", " y))))
    ;; )))
    (refresh-tasks)
    ))

(defn startup []
  (let [raphael (.-Raphael js/window)
        new-paper (raphael "paper" 1920 1080)]
    (swap! paper (fn [_] new-paper))
    (swap! data (fn [_] {:results #{}}))
    (init-intro!)
    ))
