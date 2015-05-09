(ns ecos.routes.content
  (:use [compojure.core]
        [ring.adapter.jetty])
  (:require [cheshire.core :as json]
            [clj-time.core :as t]
            [clojure.tools.reader.edn :as reader]
            [cognitect.transit :as transit]
            [compojure.route :as route]
            [compojure.response :as response]
            [ecos.datomic :as ecod]
            [ecos.models.user :as users]
            [ecos.views.content :as content-views]
            [ring.middleware.stacktrace :as stacktrace]
            [ring.middleware.json :as json-mid])
  (:import [java.io ByteArrayOutputStream File]
           [java.util Date]))

(defn write [val & [verbose?]]
  (let [baos (ByteArrayOutputStream.)
        w    (transit/writer baos (if verbose? :json-verbose :json))
        _    (transit/write w val)
        ret  (.toString baos)]
    (.reset baos)
    ret))

(defn path-for [id]
  (str "resources/private/docs/" id ".edn"))

(defn last-touched [path]
  (Date. (.lastModified (File. path))))

(defroutes content-routes*
  (GET "/test/:author" [author]
    {:header {"Content-Type" "application/transit+json"}
     ;; TODO: Exposing datalog query interface to a security-filtered
     ;; datomic db would be ideal, but sending 20MB of datoms across
     ;; the wire and importing into DS is a bad idea.
     :body   (write (content-views/recent "sean@bushi.do" 20))})
  (GET "/recent.trn" [:as request]
    
    (let [email (get-in request [:query-params "email"])
          limit (Integer/parseInt (get-in request [:query-params "limit"] "20"))]
      (println (pr-str email) ":" (pr-str limit))
      {:header {"Content-Type" "application/transit+json"}
       :body   (write {:stories (content-views/recent email limit)
                       :following (users/email->following (ecod/default-db) email)})}))
  ;;(GET "/" [] (content-views/html-threads "sgrove" nil))
  (GET "/" [] (content-views/html-threads "sgrove" nil))
  ;; (GET "/:author/:limit" [author limit]
  ;;   (content-views/html-threads author limit))
  (GET "/dev.json" []
    {:body {:data (pr-str (content-views/recent 20))}})
  (route/resources "/"))

(def content-routes
  (-> content-routes*
      (json-mid/wrap-json-body {:keywords? true})
      stacktrace/wrap-stacktrace))
