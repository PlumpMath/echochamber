(ns ecos.models.user
  (:require [ecos.datomic :as ecod]
            [ecos.models.common :as common]
            [datomic.api :refer [db q] :as d]))

(def all
  (partial common/all :user/email))

(defn by-email [db email]
  (d/entity db
            (first (d/q '[:find [ ?eid]
                          :where
                          [?eid :user/email ?email]]
                        db
                        email))))

(defn following [db user-id]
  (map (partial d/entity db)
       (d/q '[:find [?eid ...]
              :in $ ?user-id
              :where
              [?eid :fl/user ?user-id]
              [?eid :fl/ext-username ?un]
              [?eid :fl/source ?src]]
            db
            user-id)))

(defn email->following [db email]
  (->> (by-email db email)
       :db/id
       (following db)
       (map :fl/ext-username)))

#_(d/q '[:find ?un ?src
       :in $ ?user-id
       :where
       [?eid :fl/user ?user-id]
       [?eid :fl/ext-username ?un]
       [?eid :fl/source ?src]]
     db
     user-id)

