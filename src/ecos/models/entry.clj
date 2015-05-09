(ns ecos.models.entry
  (:require [clojure.edn :as edn]
            [ecos.datomic :as impd]
            [ecos.models.common :as common]
            [datomic.api :refer [db q] :as d]))

(defn valid-email? [value]
  (boolean (and (string? value)
                (re-find #"@" value))))

(def type-validators
  {:field.type/email     valid-email?
   :field.type/string    string?
   :field.type/text      string?
   :field.type/integer   integer?
   :field.type/float     float?
   :field.type/boolean   #(or (= % true) (= % false))
   :field.type/date      false
   :field.type/date-time false
   :field.type/file      false
   :field.type/tags      false
   :field.type/select    false})

(defn valid-type? [entity field]
  (if (:entity/serialized? entity)
    true ;; Trust anything serialized has right type
    ;; Dangerous bifurcation due to entity vs maps
    (let [value      (:entry/value entity)
          field-type (get-in field [:field/type])
          validator  (get type-validators field-type)
          required?  (:field/required? field)]
      (if (and (not required?)
               (nil? value))
        true
        (do
          (assert validator (str field-type " has no validator"))
          (validator value))))))

(defn valid? [entry field]
  (and (valid-type? entry field)
       (if (get-in entry [:entry/field :field/required?])
         (not (nil? (:entry/value entry)))
         true)))

(def all
  (partial common/all :entry/value))

(def create! common/create!)

(defn value [ent]
  (edn/read-string (:entry/value ent)))

(defn field [db ent-or-map]
  (let [field (:entry/field ent-or-map)]
    (cond
     ;; Entity, retrieve via entity API
     (not (integer? field)) field
      ;; Plain map, retrieve field via datomic query
     (integer? field) (d/entity db field))))

(defn save! [conn db ent]
    (assert (valid? ent (field ent)) "Entity is not valid for saving")
    ;;(d/transact conn [ent])
    )

(defn save-all! [conn db ents]
  (let [ents-and-fields (partition 2 (interleave ents (map (partial field db) ents)))]
    (assert (every? (partial apply valid?) ents-and-fields) "Entity collection is not valid for saving"))
  (let [processed-ents (map (fn [ent]
                              (as-> ent ent
                                    (into {} ent)
                                    (if (:entry/serialized? ent)
                                      ent
                                      (update-in ent [:entry/value] pr-str))
                                    (dissoc ent :entry/serialized?))) ents)]
    (println (pr-str processed-ents))
    (d/transact conn processed-ents)))
