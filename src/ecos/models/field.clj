(ns ecos.models.field
  (:require [ecos.datomic :as impd]
            [ecos.models.common :as common]
            [datomic.api :refer [db q] :as d]))

(def all
  (partial common/all :field/name))

(def create! common/create!)
