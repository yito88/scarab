(ns scarab.op
  (:require [scarab.record :as r])
  (:import (com.scalar.database.api Consistency
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
  ([namespace table keys]
   (prepare-get namespace table keys :linearizable))

  ([namespace table keys cl]
   (let [pk  (r/make-keys (first keys))
         ck  (r/make-keys (second keys))]
     (-> (Get. pk ck)
         (.forNamespace namespace)
         (.forTable table)
         (.withConsistency (consistency-level cl))))))

(defn prepare-put
  ([namespace table keys values]
   (prepare-put namespace table keys values :linearizable))

  ([namespace table keys values cl]
   (let [pk     (r/make-keys (first keys))
         ck     (r/make-keys (second keys))
         col-vals (r/make-values values)
         put    (-> (Put. pk ck)
                    (.forNamespace namespace)
                    (.forTable table)
                    (.withConsistency (consistency-level cl)))]
     (for [v col-vals]
       (.withValue put v)))))
