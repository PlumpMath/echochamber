(ns ecos.models.follow
  (:require [clojure.edn :as edn]
            [ecos.datomic :as ecod]
            [ecos.models.common :as common]
            [datomic.api :refer [db q] :as d]))

(defn by-username [db username]
  (->> (d/q '[:find [?eid ...]
              :in $ ?un
              :where
              [?uid :user/username ?un]
              [?eid :fl/user ?uid]
              [?eid :fl/source ?src]
              [?eid :fl/ext-username ?ext-un]]
            db
            username)
       (map (partial d/entity (ecos.datomic/default-db)))))

(defn save! [conn ent]
  (d/transact conn [ent]))

(defn save-all! [conn ents]
  (d/transact conn ents))

