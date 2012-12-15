(ns nightbyday.scenes)

(defn day1 []
  {:background {:image "img/village.png" :size [1920 1080]}
   :tasks [{:id :investigate :name "Investigate crime scene"
            :tasks [{:id :examine-body :name "Examine body"}
                    {:id :examine-eyes :name "Examine eyes"}
                    {:id :examine-knife :name "Examine knife"}
                    {:id :examine-footprint :name "Examine footprint"}]}
           {:id :talk-to-witness :name "Talk to witness"}
           {:id :talk-to-police :name "Talk to police"}
           {:id :investigate-victim-home :name "Examine victim's home"}]
   :objects [{:position [650 600]
              :image "img/body1.png"
              :size [431 255]
              :scale 0.2
              :name "Body"
              :description "The body of the victim, a middle-aged man, lies on the edge of the square. The eyes are missing. There is severe bruising in the throat. There is a gaping whole in the stomach. The entrails have spilled out. He is obviously dead."}
             {:position [1000 620]
              :image "img/man1.png"
              :size [236 438]
              :flip true
              :scale 0.2
              :name "Joe"
              :description "Tall man."}
             {:position [1130 460]
              :image "img/man2.png"
              :size [168 448]
              :flip true
              :scale 0.15}
             {:position [500 880]
              :image "img/man3.png"
              :size [266 448]
              :scale 0.3}
             ]})
