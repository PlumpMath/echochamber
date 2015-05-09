(ns ecos.models.common
  (:require [ecos.datomic :as ecod]
            [datomic.api :refer [db q] :as d]))

(defn create! [conn field-attrs]
  (let [temp-id (d/tempid :db.part/user)]
    (d/transact conn [(assoc field-attrs :db/id temp-id)])))

(defn all [field-name db]
  (map (fn [eid]
         (d/entity db eid))
       (d/q '[:find [?eid ...]
              :in $ ?field-name
              :where [?eid ?field-name]]
            db
            field-name)))

(defn valent [ent]
  (update-in ent [:entry/value] read-string))

(defn expand-ent [ent]
  (let [mp (into {} ent)]
    (merge {:field/id (get-in ent [:entry/field :db/id])}
           (select-keys (:entry/field ent) [:field/name :field/type :field/position])
           (select-keys (valent mp) [:entry/value :db/id])
           (select-keys ent [:db/id]))))

(defn load-data! [conn]
  (let [u-id (d/tempid :db.part/user)
        user {:db/id u-id :user/email "sean@bushi.do" :user/username "sgrove" :user/name "Sean Grove" :user/score 0}
        fl1  {:db/id (d/tempid :db.part/user) :fl/user u-id :fl/ext-username "richhickey" :fl/source :fl.source/hn}
        fl2  {:db/id (d/tempid :db.part/user) :fl/user u-id :fl/ext-username "brandonbloom" :fl/source :fl.source/hn}
        fl3  {:db/id (d/tempid :db.part/user) :fl/user u-id :fl/ext-username "swannodette" :fl/source :fl.source/hn}
        fl4  {:db/id (d/tempid :db.part/user) :fl/user u-id :fl/ext-username "peterhunt" :fl/source :fl.source/hn}
        fl5  {:db/id (d/tempid :db.part/user) :fl/user u-id :fl/ext-username "vjeux" :fl/source :fl.source/hn}
        fl6  {:db/id (d/tempid :db.part/user) :fl/user u-id :fl/ext-username "chenglou" :fl/source :fl.source/hn}
        fl7  {:db/id (d/tempid :db.part/user) :fl/user u-id :fl/ext-username "stcredzero" :fl/source :fl.source/hn}]
    (d/transact conn [user fl1 fl2 fl3 fl4 fl5 fl6 fl7])))

(defn historical-values [db eid attr]
  (filter last (sort-by #(nth % 3) (d/q '[:find ?attr ?v ?tx ?op
                                   :in $ ?e
                                   :where [?e ?attr ?v ?tx ?op]]
                                 (d/history db)
                                 eid))))
