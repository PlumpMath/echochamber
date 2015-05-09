(ns ecos.models.ta
  (:require [ecos.datomic :as impd]
            [datomic.api :refer [db q] :as d]))

;; This will work as long as other things don't get a document id
(defn all [db]
  (impd/touch-all '{:find [?t]
                    :where [[?t :layer/name]]}
                  db))

(comment
  (let [[doc-id & layer-ids] (pcd/generate-eids (pcd/conn) 4)]
    (d/transact (pcd/conn)
                [{:db/id doc-id
                  :document/name "Test Document 1"}
                 {:db/id (first layer-ids)
                  :document/id doc-id
                  :layer/name "Test Layer 1"
                  :layer/fill "red"}
                 {:db/id (second layer-ids)
                  :document/id doc-id
                  :layer/name "Test Layer 2"
                  :layer/fill "blue"}
                 {:db/id (last layer-ids)
                  :document/id doc-id
                  :layer/name "Test Layer 3"
                  :layer/fill "green"}])))
