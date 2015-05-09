(ns ecos.models.instance
  (:require [ecos.datomic :as impd]
            [ecos.models.common :as common]
            [datomic.api :refer [db q] :as d]))

(def all
  (partial common/all :instance/model))

(def create! common/create!)

(defn full-instance [inst]
  (let [entries (:entry/_instance inst)
        full-entries (map common/expand-ent entries)]
    (sort-by :field/name full-entries)))
