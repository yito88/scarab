(ns scarab.op
  (:require [scarab.record :as r])
  (:import (com.scalar.database.api Consistency
                                    Delete
                                    Get
                                    Isolation
                                    Put
                                    Result)))

(def consistency-levels
  {:sequential   Consistency/SEQUENTIAL
   :eventual     Consistency/EVENTUAL
   :linearizable Consistency/LINEARIZABLE})

(defn consistency-level
  [cl]
  (get consistency-levels cl))

(defn prepare-get
  [{:keys [namespace table pk ck cl] :or {cl :eventual}}]
  (let [pk  (r/make-keys pk)
        ck  (r/make-keys ck)]
    (-> (Get. pk ck)
        (.forNamespace namespace)
        (.forTable table)
        (.withConsistency (consistency-level cl)))))

(defn prepare-delete
  [{:keys [namespace table pk ck cl] :or {cl :eventual}}]
  (let [pk  (r/make-keys pk)
        ck  (r/make-keys ck)]
    (-> (Delete. pk ck)
        (.forNamespace namespace)
        (.forTable table)
        (.withConsistency (consistency-level cl)))))

(defn prepare-put
  [{:keys [namespace table pk ck values cl] :or {cl :eventual}}]
  (let [pk     (r/make-keys pk)
        ck     (r/make-keys ck)
        col-vals (r/make-values values)
        put    (-> (Put. pk ck)
                   (.forNamespace namespace)
                   (.forTable table)
                   (.withConsistency (consistency-level cl)))]
    (for [v col-vals]
      (.withValue put v))))
