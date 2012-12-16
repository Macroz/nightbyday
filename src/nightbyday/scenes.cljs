(ns nightbyday.scenes)

(defn day1 []
  {:background {:image "img/village.png" :size [1920 1080]}
   :tasks [{:id :findoutwhathappened :name "Find out what happened" :known? true
            :tasks [{:id :talk-alexthimblewood :name "Talk to the police" :known? true}]
            :reveal [:helppolice :investigate :talk-to-witness :investigate-victim-home]}
           {:id :helppolice :name "Help police to investigate the murder" :known? false
            :tasks [{:id :investigate :name "Investigate the crime scene" :known? false
                     :tasks [{:id :examine-body :name "Examine the body"}
                             {:id :examine-guts :name "Examine the guts"}
                             {:id :examine-eyes :name "Examine the eyes"}
                             {:id :examine-knife :name "Examine the knife"}
                             {:id :examine-footprints :name "Examine the footprints"}]}
                    {:id :talk-to-witness :name "Talk to witnesses" :known? false}
                    {:id :investigate-victim-home :name "Examine the victim's home" :known? false}]}]
   :objects [
             ;; houses
             {:id :smilingslothinn
              :position [743 269]
              :size [233 202]
              :name "Smiling Sloth Inn"
              :description "Smiling Sloth Inn is the best, and in fact the only, place for visitors to stay in Maple-on-river."}
             {:id :stables
              :position [1106 386]
              :size [305 110]
              :name "Stables"
              :description "These are the stables of the Smiling Sloth Inn. There are no horses here today. The stableboy must be busy with something else, because he is not here either."}
             {:id :tallhouse
              :position [173 159]
              :size [434 470]
              :name "Tall house"
              :description "This is the tallest house in Maple-on-river. Three households live there. There is a shed next to it."}
             {:id :shed
              :position [614 420]
              :size [90 130]
              :name "Shed"
              :description "A small shed stands next to the tall house."}
             {:id :policestation
              :position [0 400]
              :size [420 600]
              :name "Police Station"
              :description "The police station guards the square in the middle of Maple-on-river. Regularly one policeman stays here unless there is an emergency."}
             {:id :generalstore
              :position [1430 330]
              :size [500 340]
              :name "General Store"
              :description "The Good Ol' General Store is the main supplier of goods in Maple-on-river. Most people have their own livestock and gardens for growing vegetables, but the goods produced elsewhere come here. There is a delivery of new goods every week. You arrived with the delivery truck yesterday."}
             {:id :johnshouse
              :position [1400 630]
              :size [550 450]
              :name "John's House"
              :description "John the Farmer lives here. He farms most of the land the village owns. His wife Mary is a veterinarian who also herds cattle, sheep and horses with their underlings."}

             ;; crime scene
             {:id :body
              :position [650 600]
              :image "img/body1.png"
              :size [431 255]
              :scale 0.2
              :name "Body"
              :description "The body of a murder victim lies on the edge of the square."
              :examine "The body of a middle-aged man, looks like the mutilated corpse of David Winterfall. The eyes are missing, having been dug out from their sockets. There is severe bruising in the throat area. There is also a gaping whole in the stomach, from where the guts have spilled out. He is obviously dead."}
             {:id :eyes
              :position [705 590]
              :image "img/eyes1.png"
              :size [102 57]
              :scale 0.2
              :name "Eyes"
              :description "A pair of presumably human eyes lies in a pool of blood."
              :examine "The eyes probably belong to the victim. They have been dug out and tossed aside."}
             {:id :guts
              :position [690 620]
              :image "img/blood1.png"
              :size [102 57]
              :scale 0.3
              :name "Guts"
              :description "The guts of the victim have spilled out from his stomach."
              :examine "The gaping wound looks like a cut with a blade."}
             {:id :knife
              :position [420 680]
              :image "img/knife1.png"
              :size [89 48]
              :scale 0.3
              :name "Knife"
              :description "A bloody knife lies on the ground."
              :examine "It looks like a kitchen knife. Possibly the murder weapon."}

             {:id :footprints
              :position [500 640]
              :image "img/tracks1.png"
              :size [327 167]
              :scale 0.25
              :name "Footprints"
              :description "Looks like footprints on the ground."
              :examine "There is some dried up blood in the ground. Perhaps the murdered fled this way."}

             ;; people
             {:id :johngoodfellow
              :position [1000 620]
              :image "img/man1.png"
              :size [236 438]
              :flip true
              :scale [0.22 0.27]
              :name "Farmer"
              :description "John Goodfellow is a tall man with a booming voice. He is farmer by profession and lives just next to the village square."}
             {:id :alexthimblewood
              :position [755 540]
              :image "img/man2.png"
              :size [168 448]
              :flip true
              :scale 0.2
              :types #{:person}
              :name "Police Officer"
              :description "A burly looking man standing in a police officer's uniform."
              :examine "Alex Thimblewood is the residing police officer of Maple-on-river. He is examining the crime scene, looking very concerned."
              :talk "I'm afraid I have some sad news. David, the landlord of this tall building, was murdered last night. I heard you are a special detective back at the capital. Might you offer assitance in this matter? Never in my long career have I seen such a horrible crime, let alone in this peaceful village."}
             {:id :eyrikoxhead
              :position [1130 460]
              :image "img/man2.png"
              :size [168 448]
              :flip true
              :scale 0.15
              :name "Innkeeper"
              :description "Eyrik Oxhead is the owner of the Smiling Sloth Inn. He has come out to see what the commotion is about."}
             {:id :peterpaulson
              :position [500 880]
              :image "img/man3.png"
              :size [266 448]
              :scale 0.3
              :name "Councillor"
              :description "Peter Paulson is an esteemed villager and head of the city council."}
             ]})
