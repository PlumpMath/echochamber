(ns ecos.datomic.schema
  (:require [ecos.datomic :as impd]
            [datomic.api :refer [db q] :as d]))

(defn attribute [ident type & {:as opts}]
  (merge {:db/id (d/tempid :db.part/db)
          :db/ident ident
          :db/valueType type
          :db.install/_attribute :db.part/db}
         {:db/cardinality :db.cardinality/one}
         opts))

(defn function [ident fn & {:as opts}]
  (merge {:db/id (d/tempid :db.part/user)
          :db/ident ident
          :db/fn fn}
         opts))

(defn enum [ident]
  {:db/id (d/tempid :db.part/user)
   :db/ident ident})

(def schema
  (vec
   (concat
    ;; Local Users
    [(attribute :user/email
                :db.type/string)
     (attribute :user/username
                :db.type/string)
     (attribute :user/name
                :db.type/string)
     (attribute :user/score
                :db.type/long)]

    ;; Follow-lists
    [(attribute :fl/user
                :db.type/ref)
     (attribute :fl/ext-username
                :db.type/string)
     (attribute :fl/source
                :db.type/ref)]

    ;; Sources
    [(enum :fl.source/hn)
     (enum :fl.source/reddit)
     (enum :fl.source/lobsters)]

    ;; Items
    [(attribute :item/title
                :db.type/string)
     (attribute :item/text
                :db.type/string)
     (attribute :item/url
                :db.type/string)
     (attribute :item/score
                :db.type/long)
     (attribute :item/by
                :db.type/ref)
     (attribute :item/time
                :db.type/instant)
     (attribute :item/source
                :db.type/string)
     (attribute :item/ext-id
                :db.type/string)
     (attribute :item/ext-parent
                :db.type/string)
     (attribute :item/parent
                :db.type/ref)
     (attribute :item/type
                :db.type/ref)
     (attribute :item/signature
                :db.type/string
                :db/unique :db.unique/identity)

     (enum :item.type/story)
     (enum :item.type/poll)
     (enum :item.type/job)
     (enum :item.type/comment)]
    
    ;; Sessions
    ;;;; No logins at the moment, so we'll use this to identify users
    ;;;; chats rely on this, is it a good idea? Nice to have something stable across tabs
    [(attribute :session/uuid
                :db.type/uuid
                :db/index true)

     (attribute :session/client-id
                :db.type/string)]

    ;; Dummy attrs
    [(attribute :dummy
                :db.type/ref)
     (enum :dummy/dummy)])))

(defonce schema-ents (atom nil))

(defn enums []
  (set (map :db/ident (filter #(= :db.type/ref (:db/valueType %))
                              @schema-ents))))

(defn get-ident [a]
  (:db/ident (first (filter #(= a (:db/id %)) @schema-ents))))

(defn get-schema-ents [db]
  (impd/touch-all '{:find [?t]
                   :where [[?t :db/ident ?ident]]}
                 db))

(defn ensure-schema
  ([] (ensure-schema (impd/conn)))
  ([conn]
   (let [res @(d/transact conn schema)
         ents (get-schema-ents (:db-after res))]
     (reset! schema-ents ents)
     res)))

(defn init []
  (ensure-schema))
;; (init)

