(ns ecos.views.content
  (:require [clojure.string :as string]
            [datomic.api :as d]
            [ecos.datomic :as ecod]
            [ecos.views.common :as common]
            [ecos.models.follow :as fl]
            [ecos.models.item :as items]
            [ecos.models.user :as users])
  (:use [hiccup.core :refer :all]))

(defn item-view [item & [level]]
  [:div [:strong (:item/by item)] ": " (pr-str (:item/time item))
   [:div (:item/text item)
    [:div.item-children {:style "padding-left: 25px"}
     (map item-view (:children item) (repeat (inc (or 0 level))))]]])

(defn story-view [story]
  [:div
   [:h2.title [:a {:href (str "https://news.ycombinator.com/item?id=" (get-in story [:story :item/ext-id]))
                   :target "_blank"}
               (get-in story [:story :item/title])]]
   [:div.items
    (item-view (first (:children story)))]])

(defn html-threads [user & [limit]]
  (html
   (let [db    (ecod/default-db)
         following (map :fl/ext-username (fl/by-username db user))
         ;; items (take (or limit 5)
         ;;             (map (partial items/reified-item-thread db)
         ;;                  (items/by-author db author)))
         items (reduce
                (fn [run {:keys [story children] :as new-item}]
                  (if-let [idx (first (filter identity (map-indexed (fn [idx item]
                                                                      (when (= (:item/title story) (get-in item [:story :item/title]))
                                                                        idx)) run)))]
                    (update-in run [idx :children] conj children)
                    (conj run new-item)))
                []
                [])]
     (common/layout
      [:input.history {:style "display:none;"}]
      [:div#player-container]
      [:div#app-container]
      [:div.debugger-container]
      [:div#app]
      [:div#ds]
      [:link.css-styles {:rel "stylesheet", :href "/css/styles.css"}]
      [:div.stories
       (map story-view items)]
      (if (= (System/getenv "PRODUCTION") "true")
        [:script {:type "text/javascript" :src "/js/bin/main.js"}]
        (if false
          [:script {:type "text/javascript" :src "/js/bin-debug/main.js"}]
          (list
           [:script {:type "text/javascript" :src "/js/vendor/react-0.10.0.js"}]
           [:script {:type "text/javascript" :src "/js/bin-debug/goog/base.js"}]
           [:script {:type "text/javascript" :src "/js/bin-debug/main.js"}]
           [:script {:type "text/javascript"}
            "goog.require(\"ecos.client.core\");"])))))))

(defn transit-test [username]
  (let [db (ecod/default-db)
        fl (map :fl/ext-username (fl/by-username db "sgrove"))]
    (mapcat (fn [author]
              (pmap (fn [ent]
                      (let [e  (d/entity db ent)
                            t  (into {} (d/touch e))]
                        (read-string (pr-str t))))
                    (d/q '[:find [?eid ...]
                           :in $ ?by
                           :where
                           [?eid :item/by ?by]]
                         db
                         author)))
            fl)))

(defn app [name]
  (html (html-threads "sgrove")))

(defn recent [email limit]
  (let [db       (ecod/default-db)
        conn     (ecod/conn)
        followed (users/email->following db email)
        bys      `(~'or ~@(mapv (fn [username] ['?eid :item/by username]) followed))
        ents     (->> (d/q {:find '[[?eid ...]]
                            :in '[$ ?bys]
                            :where [bys]}
                           db
                           bys)
                      (map (partial d/entity db))
                      (sort-by :item/time)
                      reverse
                      (take limit))
        h        (fn [node]
                   [(:item/by node) (:item/time node) (or (:item/title node)
                                                          (:item/text node))]
                   [(select-keys node (keys node))])]
    
    (items/item-eids->threads db (map :db/id ents) h;;identity
                              )))
