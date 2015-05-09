(ns ecos.models.model
  (:require [ecos.datomic :as impd]
            [ecos.models.common :as common]
            [datomic.api :refer [db q] :as d]))

(def all
  (partial common/all :model/name))

(def create! common/create!)

(defn fields [model]
  (:field/_model model))

(defn instances [model]
  (:instance/_model model))
