(ns ecos.models.item
  (:require [clojure.edn :as edn]
            [ecos.datomic :as ecod]
            [ecos.models.common :as common]
            [datomic.api :refer [db q] :as d]))

(def all
  (partial common/all :item/by))

(def parent
  ;; ?a is a parent of ?b
  '[[[parent ?a ?b]
     [?b :item/parent ?a]]])

(def ancestor
  ; ?a is an anscestor of ?b
  '[[[ancestor ?a ?b]
     [parent ?a ?b]]
    [[ancestor ?a ?b]
     [parent ?a ?x]
     [ancestor ?x ?b]]])

(def rules
  (concat parent ancestor))

(defn item-ancestors [db id]
  (loop [id id
         rs []]
    (if-let [id (get (first (d/datoms (ecod/default-db) :aevt :item/parent id)) :v)]
      (recur id (conj rs id))
      (map (comp #(select-keys % [:item/by :item/text :item/type :item/parent :item/time]) (partial d/entity (ecod/default-db))) rs))))

(defn item-descendents [db id]
  (d/q '[:find ?anc-id ?anc-by ?type ?text ?parent ?time
         :in $ % ?eid
         :where
         [ancestor ?eid ?anc-id]
         [?anc-id :item/by ?anc-by]
         [?anc-id :item/text ?text]
         [?anc-id :item/type ?type]
         [?anc-id :item/parent ?parent]
         [?anc-id :item/time ?time]]
       (ecod/default-db)
       rules
       id))

(defn item [db id]
  (d/q '[:find [?eid ?time ?type ?by]
         :in $ % ?eid
         :where
         [?eid :item/time ?time]
         [?eid :item/by ?by]
         [?eid :item/type ?type]]
       (ecod/default-db)
       rules
       id))

(defn item-story-id [db id]
  (loop [id id]
    (if-let [id (get (first (d/datoms (ecod/default-db) :aevt :item/parent id)) :v)]
      (recur id)
      id)))

(defn by-author* [db author]
  (->> (d/q '[:find ?eid ?time
              :in $ % ?by
              :where
              [?eid :item/by ?by]
              [?eid :item/time ?time]]
            (ecod/default-db)
            rules
            author)
       (sort-by second)
       reverse))

(defn by-author [db author]
  (map first (by-author* db author)))

(defn reified-item-thread [db id & keys]
  (let [ent   (d/entity db (first (item db id)))
        story (d/entity db (item-story-id db (:db/id ent)))
        keys  (or keys [:item/title :item/text :item/parent :item/by :item/time :item/ext-id])
        ks    #(select-keys % keys)]
    {:story    (ks story)
     :children (mapv (fn h [i]
                       (assoc (ks i) :children (mapv h (:item/_parent i)))) (:item/_parent story))}))

(defn item-eid->thread [node-fmt item]
  (let [ds (or ((fn h [children run]
                  (if (empty? (remove nil? children))
                    run
                    (vec (concat run (mapv node-fmt children)))))
                (:item/_parent item) nil)
               [nil])
        item-and-ds (apply conj (node-fmt item) ds)
        final ((fn h [item run]
                 (if item
                   (h (:item/parent item)
                      (conj (node-fmt item) run))
                   run))
               (:item/parent item) item-and-ds)]
    final))

(defn item-eids->threads [db eids node-fmt]
  (let [items (->> (ecod/repl-refs db
                                   #{:fl/source}
                                   (d/pull-many db
                                                '[:db/id
                                                  :item/title
                                                  :item/text
                                                  :item/url
                                                  :item/score
                                                  :item/by
                                                  :item/time
                                                  :item/source
                                                  :item/ext-id
                                                  :item/ext-parent
                                                  :item/type
                                                  :item/signature
                                                  {:item/parent ...}
                                                  {:item/_parent ...}
]
                                                eids)))]
    (mapv (partial item-eid->thread node-fmt) items)))

(def all
  (partial common/all :item/text))

#_(def stories
    (partial common/all))

(def create! common/create!)

(defn save! [conn ent]
  (d/transact conn [ent]))

(defn save-all! [conn ents]
  (d/transact conn ents))

(defn historical-values [db eid attr]
  (filter last (sort-by #(nth % 3) (d/q '[:find ?attr ?v ?tx ?op
                                   :in $ ?e
                                   :where [?e ?attr ?v ?tx ?op]]
                                 (d/history db)
                                 eid))))
