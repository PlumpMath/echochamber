(ns ecos.server
  (:use [compojure.core])
  (:require [figwheel.client :as fw]
            [ring.adapter.jetty :refer [run-jetty]]
            [ecos.routes.content :as content-routes]
            [ring.middleware.session :as ring-session]
            [compojure.handler :as handler])
  (:import [org.eclipse.jetty.server Server])
  (:gen-class))

;;******************************************************************************
;;  Server Routes & Connections
;;******************************************************************************

(def ^:no-check app-routes
  (routes (var content-routes/content-routes)))

(def app
  (handler/site app-routes))


(defonce servers (atom {}))

(defn create-server! [port background? & [ssl?]]
  (print (str "Creating server on port " port (when background? " in the background") " with " app))
  (run-jetty (var app) {:port port :join? (not background?)}))

(defn stop! [name]
  (.stop (get @servers name)))

(defn restart! [name]
  (.stop  (get @servers name))
  (.start (get @servers name)))

(defn quick-start! [& [port background? ssl?]]
  (swap! servers assoc :default (create-server! (or port 4000) (or background? true) ssl?)))

(defn start-named! [name & [port background? ssl?]]
  (swap! servers assoc name (create-server! (or port 4000) (or background? true) ssl?)))

(defn init [& args]
  (start-named! :primary (or (when-let [port (first args)] (Integer/parseInt port)) 4001) false false))

;;******************************************************************************
;;  ClojureScript Helpers
;;******************************************************************************
(comment
  ;; First, make sure you've done a clean, successful recompile of your cljs
  ;; Evaluate this form
  (def repl-env (reset! cemerick.austin.repls/browser-repl-env
                        (cemerick.austin/repl-env)))
  ;; Now re-evaluate views/common and views/content (open them up and use C-c C-k)
  ;; Then start the server (don't visit the page yet)
  (db/connect!)
  (start-named! :auto-start 4000 true false)
  ;; Now start a clojurescript repl
  (cemerick.austin.repls/cljs-repl repl-env)
  ;; Now visit the page in the browser, and your repl should "just work"
  (init)
  )


