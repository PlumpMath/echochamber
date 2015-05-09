(ns ecos.init
  (:require ecos.datomic
            ecos.server))

(def init-fns [#'ecos.datomic/init
               #'ecos.server/init])

(defn pretty-now []
  (.toLocaleString (java.util.Date.)))

(defn init []
  (doseq [f init-fns]
    (println (pretty-now) f)
    (f)))

(defn -main []
  (init)
  (println (pretty-now) "done"))
