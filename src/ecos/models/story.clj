(ns ecos.models.story
  (:require [ecos.datomic :as impd]
            [ecos.models.common :as common]
            [datomic.api :refer [db q] :as d]))

(def all
  (partial common/all :story/title))

(def create! common/create!)
