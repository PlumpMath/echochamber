(ns ecos.scrapers.hn
  (:require [clj-http.client :as http]
            [ecos.datomic :as ecod]
            [datomic.api :as d]))

;; TODO: Normalize HN text. Or just use _dangerouslySetHTML?
(def defbase
  "https://hacker-news.firebaseio.com/v0")

(defn map-keys [m mappings]
  (into {} (remove (comp nil? second)
                   (map (fn [[s d]]
                          [d (get m s)]) mappings))))

(defn private-item? [item]
  (nil? (:time item)))

(defn normalize-item [item]
  (when-not (private-item? item)
    (-> item
        (update-in [:time] #(java.util.Date. (long (* 1000 %))))
        (update-in [:id] pr-str)
        (update-in [:parent] pr-str)
        (update-in [:type] {"job"     :item.type/job
                            "story"   :item.type/story
                            "comment" :item.type/comment
                            "poll"    :item.type/poll
                            "pollopt" :item.type/poll})
        (assoc-in [:signature] (str "hn." (:id item)))
        (map-keys {:time      :item/time
                   :title     :item/title
                   :id        :item/ext-id
                   :score     :item/score
                   :url       :item/url
                   :by        :item/by
                   :text      :item/text
                   :type      :item/type
                   :parent    :item/ext-parent
                   :signature :item/signature}))))

(defn get-item* [base id & [debug]]
  (http/get (str base "/item/" id ".json")
            {:as               :json
             :debug?           debug
             :debug            debug
             :coerce           :always
             :throw-exceptions false}))

(def get-item
  (comp normalize-item :body get-item*))

(defn item-with-descendents [base id]
  (let [item (:body (get-item* base id))
        kids (:kids item)]
    (-> item
        normalize-item
        (assoc :children (mapv (partial item-with-descendents base) (:kids item))))))

(defn item-with-ancestors [base id & child]
  (let [current (when id
                  (:body (get-item* base id)))]
    (if current
      (item-with-ancestors base (:parent current)
                           (-> current
                               normalize-item
                               (assoc :children (vec child))))
      (first child))))

(defn full-thread-for-id [base id]
  (as-> (item-with-descendents base id) item 
        (item-with-ancestors base (:item/ext-parent item) item)
        item))

(defn normalize-item-tree [parent-ent-id item]
  (let [helper (fn helper [parent-db-id item & [level]]
                 (let [db-id (:db/id item (d/tempid :db.part/user))
                       ;; dead? checks for both dead-and-private stories
                       dead? (< (count (keys item)) 3)]
                   (dotimes [n (or level 0)]
                     (print "\t"))
                   (when (pos? (or level 0)) (print "|-"))
                   (println db-id (when parent-db-id (str " -> " parent-db-id)))
                   (as-> item item
                         (assoc item :db/id db-id)
                         (if parent-db-id
                           (assoc item :item/parent parent-db-id)
                           item)
                         (update-in item [:children] #(mapv (partial helper (when-not dead? db-id)) % (repeat (inc (or level 0))))))))]
    ;; Should we try to find the parent for the root item as well?
    (vec (mapcat (fn helper [item]
                   (concat [(dissoc item :children)] (mapcat helper (:children item)))) [(helper parent-ent-id item)]))))

(defn get-user* [base user-name]
  (http/get (str base "/user/" user-name ".json")
            {:as :json}))

(def get-user
  (comp :body get-user*))

(defn user-submitted [base user-name & [except-ids]]
  (let [ids          (:submitted (get-user base user-name))
        filtered-ids (remove (comp (set (or except-ids [])) str) ids)
        items        (vec (pmap (fn [id]
                                  (println id "|")
                                  (full-thread-for-id defbase id)) filtered-ids))]
    ;; TODO: Remove private stories
    (->> items
         (mapcat (partial normalize-item-tree nil))
         (filter #(> (count (keys %)) 2) )
         vec)))

