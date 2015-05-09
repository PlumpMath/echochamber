(ns ecos.services.scraper
  (:require [clojure.edn :as edn]
            [ecos.datomic :as ecod]
            [ecos.models.item :as items]
            [ecos.models.follow :as follow]
            [ecos.scrapers.hn :as hn]
            [datomic.api :refer [db q] :as d]))

(def services
  {:fl.source/hn hn/user-submitted})

(def transacted
  (atom {:pending {}
         :stored  {}}))

(defn scrape! [conn db id]
  (hn/normalize-item-tree nil (hn/full-thread-for-id hn/defbase id)))

(defn scrape-user! [conn db scraper username]
  (let [existing-ids (map (comp :item/ext-id (partial d/entity db) :db/id)
                          (items/all db))
        _            (println (count existing-ids) " existing ids in db.")
        user-items   (scraper hn/defbase username existing-ids)]
    (swap! transacted assoc-in [:pending username] user-items)
    (items/save-all! conn user-items)
    (swap! transacted update-in [:pending] dissoc username)))

(defn scrape-followers! [conn db username]
  (let [following (follow/by-username db username)]
    (dorun
     (pmap (fn [f]
             (let [scraper      (get services (:fl/source f))
                   ext-username (:fl/ext-username f)]
               (scrape-user! conn db scraper ext-username))) following))))

