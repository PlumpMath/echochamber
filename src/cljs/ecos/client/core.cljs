(ns ecos.client.core
  (:require ;;[ankha.core :as ankha]
            [cognitect.transit :as t]
            [cljs.reader :as reader]
            [clojure.string :as s]
            [datascript :as d]
            [figwheel.client :as fw]
            ;;[ecos.client.example-data :as data]
            [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true])
  (:import [goog.net XhrIo]))

(enable-console-print!)

(def schema {})

(defn init-db [initial-entities]
  (let [conn (d/create-conn schema)]
    (d/transact! conn initial-entities)
    conn))

(defn reset-db! [db-atom initial-entities]
  (reset! db-atom @(init-db initial-entities))
  db-atom)

(defn datom->transaction [datom]
  (let [{:keys [a e v tx added]} datom]
        [(if added :db/add :db/retract) e a v]))

(def r (t/reader :json))

(defn get-data [url cb]
  (XhrIo.send (str "/" url)
              (fn [e]
                (let [xhr (.-target e)]
                  (cb (.getResponseText xhr))))))

;; (get-data "ttest"
;;           (fn [res]
;;             (println "JSON.parse")
;;             (println res)
;;             (time
;;              (dotimes [x 50000]
;;                (.parse js/JSON res)))))

(defn update-ds! [status]
  (let [el (js/document.getElementById "ds")
        new-el (js/document.createElement "div")]
    (set! (.-innerText new-el) status)
    (.appendChild el new-el)))

#_(update-ds! "Retrieving DS Initial Datoms")
#_(get-data "test/sgrove"
          (fn [res]
            ;;(println res)
            (dotimes [x 1]
              (let [datoms (do
                             (update-ds! "Reading transit")
                             (println "Transit read")
                             (time (t/read r res)))
                    _      (update-ds! (str "Done, Datom count: " (time (count datoms))))
                    db     (do
                             (update-ds! "Load datoms")
                             (time (init-db datoms)))]
                (println (d/q '[:find ?txt ?by ?
                                :in $ ?by
                                :where
                                [?eid :item/by ?by]
                                [?eid :item/text ?txt]]
                              @db
                              "sgrove"))
                (println (d/q '[:find ?txt ?by ?
                                :in $ ?by
                                :where
                                [?eid :item/by ?by]
                                [?eid :item/text ?txt]]
                              @db
                              "pg"))))))

(defn process-child! [level child]
  (let [body  js/document.body
        entry (js/document.createElement "div")]
    (set! (.. entry -style -paddingLeft) (str (* level 20) "px"))
    (set! (.-innerHTML entry) (str "By " (:item/by child) ": --> " (:item/text child) " || " (pr-str (keys child))))
    (.appendChild body entry)
    (doseq [next-child (:item/_parent child)]
      (process-child! (inc level) next-child))))

(defn process-story! [story]
  (let [body js/document.body
        title (js/document.createElement "h1")]
    #_(set! (.-innerHTML title) (:item/title story))
    #_(.appendChild body title)
    #_(doseq [child (:item/_parent story)]
      (process-child! 0 child))))

(defonce app-state
  (atom {:title    "EchoChamber"
         :loading? true
         :limit    450
         :list     "sean@bushi.do"}))

(when (empty? (:stories @app-state))
  ;;(update-ds! "Retrieving recent stories for you...")
  (get-data (str "recent.trn?email=" (:list @app-state) "&limit=" (:limit @app-state))
            (fn [res]
              (let [data (t/read r res)]
                ;;(update-ds! (pr-str (count data)))
                ;;(update-ds! (pr-str data))
                (doseq [story data]
                  (process-story! story))
                (swap! app-state merge data)
                (swap! app-state assoc :loading? false)))))

#_(let [datoms (do
               (update-ds! "Reading transit")
               (println "Transit read")
               (time (t/read r data/data)))
      _      (update-ds! (str "Done, Datom count: " (time (count datoms))))
      db     (do
               (update-ds! "Load datoms")
               (time (init-db datoms)))]
  (println (d/q '[:find ?txt ?by ?
                  :in $ ?by
                  :where
                  [?eid :item/by ?by]
                  [?eid :item/text ?txt]]
                @db
                "sgrove"))
  (println (d/q '[:find ?txt ?by ?
                  :in $ ?by
                  :where
                  [?eid :item/by ?by]
                  [?eid :item/text ?txt]]
                @db
                "pg")))
;; Building the basic structure and printing it out
(print (pr-str
        (let [items (range 0 10)]
          [:ul (for [item items]
                 [:li "Item " item])])))
;; [:ul
;;  ([:li "Item " 0]
;;   [:li "Item " 1]
;;   [:li "Item " 2]
;;   [:li "Item " 3]
;;   [:li "Item " 4]
;;   [:li "Item " 5]
;;   [:li "Item " 6]
;;   [:li "Item " 7]
;;   [:li "Item " 8]
;;   [:li "Item " 9])]


;; Adding meta-data to the [:li ...] forms doesn't change the printed
;; representation of the data (it's meta-data, after all)
(print (pr-str (let [items (range 0 10)]
                 [:ul (for [item items]
                        ^{:key (str "example-react-key-" item)} [:li "Item " item])])))
;; [:ul
;;  ([:li "Item " 0]
;;   [:li "Item " 1]
;;   [:li "Item " 2]
;;   [:li "Item " 3]
;;   [:li "Item " 4]
;;   [:li "Item " 5]
;;   [:li "Item " 6]
;;   [:li "Item " 7]
;;   [:li "Item " 8]
;;   [:li "Item " 9])]

;; Asking for meta-data on the [:ul ...] shows that there's no metadata on that vector
(print (pr-str
        (let [items (range 0 10)]
          (meta [:ul (for [item items]
                       ^{:key (str "example-react-key-" item)} [:li "Item " item])]))))
;; nil

;; But if we ask for the meta data on the first [:li ...] vector, we see it's what we would expect
(print (pr-str
        (let [items (range 0 10)]
          (meta (first (second [:ul (for [item items]
                                      ^{:key (str "example-react-key-" item)} [:li "Item " item])]))))))
;; {:key "example-react-key-0"}

(defn hn-item-link [id]
  (str "https://news.ycombinator.com/item?id=" id))

(defn hn-user-link [id]
  (str "https://news.ycombinator.com/user?id=" id))

(defn latest-activity [story]
  (let [[title replies] story]
    (reduce (fn [newest time]
              (if (and time (< (.getTime newest) (.getTime time)))
                time
                newest))
            (js/Date. 0)
            (map :item/time (flatten replies)))))

(defn reply [data owner opts]
  (reify
    om/IRender
    (render [_]
      (let [level             (:level opts)
            [reply-data & replies] data]
        (dom/div
         #js{:className "reply"
             :style #js{:marginLeft (+ 2 (* level 20))
                        ;;:backgroundColor (rand-nth ["blue" "black" "green" "pink" "red"])
                        }}
         (dom/p nil
                (dom/pre nil
                         
                         (dom/a #js{:href (hn-user-link (:item/by reply-data))
                                    :target "_blank"} (:item/by reply-data)) " on "
                                    (dom/a #js{:href   (hn-item-link (:item/ext-id reply-data))
                                               :target "_blank"}
                                           (str (:item/time reply-data))) ":"))
         (dom/div #js{:style #js{:backgroundColor (when ((set (:following @app-state)) (:item/by reply-data))
                                                    "#BBFFBB")}
:dangerouslySetInnerHTML #js{:__html (:item/text reply-data)}})
         (apply dom/div nil
                (map (fn [d]
                       (when (seq d)
                         (om/build reply d {:opts {:level (inc level)}}))) replies)))))))

(defn story [data owner opts]
  (reify
    om/IRender
    (render [_]
      (let [[story & replies] data]
        (dom/div
         nil
         (dom/h1 nil (dom/a #js{:href   (hn-item-link (:item/ext-id story))
                                :target "_blank"} (:item/title story)))
         (dom/pre nil "Submitted by " (dom/a #js{:href (hn-user-link (:item/by story))
                                                 :target "_blank"} (str (:item/by story)))
                  " with " (:item/score story) "pts")
         (apply dom/div nil
                (om/build-all reply replies {:opts {:level 0}})))))))

(defn followed-participants [followed replies]
  (set (map :item/by (filter (fn [reply]
                               (get followed (:item/by reply))) (flatten replies)))))

(defn headline [data owner opts]
  (reify
    om/IRender
    (render [_]
      (let [[_ temp] data
            ;;story (get-in temp [0 0])
            [story & replies] data]
        (if (om/get-state owner :expanded?)
          (dom/div
           nil
           
           (dom/h2 #js{:onClick (fn [event]
                                  (om/update-state! owner :expanded? not))}
                   "- "
                   (dom/a #js{:href   (hn-item-link (:item/ext-id story))
                              :target "_blank"} (:item/title story)))
           (dom/pre nil "Submitted by " (dom/a #js{:href (hn-user-link (:item/by story))
                                                   :target "_blank"} (str (:item/by story)))
                    " with " (:item/score story) "pts")
           (apply dom/div nil
                  (om/build-all reply replies {:opts {:level 0}})))
          (dom/div #js{:style #js{:cursor "pointer"}
                       :className "headline"
                       :onClick (fn [event]
                                  (om/update-state! owner :expanded? not))}
                   (let [tt (pr-str story)]
                     #_(subs tt 0 (min 200 (count tt))))
                   ;; "Latest: " (pr-str (reduce (fn [newest time]
                   ;;                              (println "(< newest time)" (< newest time))
                   ;;                              (if (and time (< (.getTime newest) (.getTime time)))
                   ;;                                time
                   ;;                                newest))
                   ;;                            (js/Date. 0) (map :item/time (flatten replies))))
                   ;;"Story: " (dom/pre nil (pr-str story))
                   "+ " (dom/a #js{:href (hn-item-link (:item/ext-id story))
                                               :target "_blank"} (:item/title story))
                   " | " (dom/a #js{:href (hn-user-link (:item/by story))
                                                         :target "_blank"} (str (:item/by story)))
                   " @ " (:item/score story) "pts"
                   " followed: " (pr-str (followed-participants (set (:following @app-state)) replies))))))))

(defn root [data owner opts]
  (reify
    om/IRender
    (render [_]
      (let [stories (group-by #(get-in % [0 :item/title]) (:stories data))
            stories (reverse (sort-by latest-activity stories))]
        (dom/div #js{:className "container"}
                 (dom/h1 nil (:title data))
                 (if (:loading? data)
                   (dom/div #js{:className "following"} (str "Loading " (:list data)))
                   (dom/div #js{:className "following"} (str "List " (:list data) " follows " (count (:following data)) " users: ") (pr-str (vec (sort-by s/lower-case (:following data))))))
                 ;;(dom/pre nil (pr-str data))
                 #_(om/build ankha/inspector data)
                 #_(dom/p nil "Most recent " (:limit data) " items")
                 (dom/hr nil)
                 ;; (let [t (map (fn [r]
                 ;;                (dom/li nil (pr-str (latest-activity r)))) stories)]
                 ;;   (apply dom/ul nil t))
                 ;; (dom/hr nil)
                 (apply dom/div nil
                        (om/build-all headline (:stories data))))))))

(defn install! [target state]
  (om/root
   root
   state
   {:target target}))

(install! (js/document.getElementById "app") app-state)

(fw/start {:server-port 3449
           :websocket-url (str "ws://"
                               "localhost:3449"
                               "/figwheel-ws")
           :on-jsload   (fn []
                          (println "FW loaded!"))})
