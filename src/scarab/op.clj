(ns scarab.op
  (:require [scarab.record :as r])
  (:import (com.scalar.db.api ConditionalExpression
                              ConditionalExpression$Operator
                              Consistency
                              Delete
                              DeleteIf
                              DeleteIfExists
                              Get
                              Isolation
                              Put
                              PutIf
                              PutIfExists
                              PutIfNotExists
                              Result
                              Scan
                              Scan$Ordering
                              Scan$Ordering$Order)))

(def consistency-levels
  {:sequential   Consistency/SEQUENTIAL
   :eventual     Consistency/EVENTUAL
   :linearizable Consistency/LINEARIZABLE})

(def exp-operator
  {:eq ConditionalExpression$Operator/EQ
   :ne ConditionalExpression$Operator/NE
   :gt ConditionalExpression$Operator/GT
   :gte ConditionalExpression$Operator/GTE
   :lt ConditionalExpression$Operator/LT
   :lte ConditionalExpression$Operator/LTE})

(defn consistency-level
  [cl]
  (get consistency-levels cl))

(defn- make-condition-exp
  [column value operator]
  (ConditionalExpression. column
                          (r/make-value column value)
                          (exp-operator operator)))

(defn prepare-get
  [{:keys [namespace table pk ck cl] :or {cl :eventual}}]
  (let [pk  (r/make-keys pk)
        ck  (r/make-keys ck)]
    (-> (Get. pk ck)
        (.forNamespace namespace)
        (.forTable table)
        (.withConsistency (consistency-level cl)))))

(defn prepare-scan
  [{:keys [namespace table pk start-ck end-ck
           inclusive-start? inclusive-end?
           ordering limit cl]
    :or {inclusive-start? true inclusive-end? true cl :eventual}}]
  (let [pk  (r/make-keys pk)
        scan (-> (Scan. pk) (.forNamespace namespace) (.forTable table))]
    (when start-ck
      (.withStart scan (r/make-keys start-ck) inclusive-start?))
    (when end-ck
      (.withEnd scan (r/make-keys end-ck) inclusive-end?))
    (when ordering
      (doseq [[name order] ordering]
        (->> (Scan$Ordering. name (if (= order :asc)
                                    Scan$Ordering$Order/ASC
                                    Scan$Ordering$Order/DESC))
             (.withOrdering scan))))
    (when limit
      (.withLimit scan limit))
    (.withConsistency scan (consistency-level cl))))

(defn prepare-delete
  [{:keys [namespace table pk ck if-exists condition cl] :or {cl :eventual}}]
  (let [pk  (r/make-keys pk)
        ck  (r/make-keys ck)
        delete (-> (Delete. pk ck)
                   (.forNamespace namespace)
                   (.forTable table)
                   (.withConsistency (consistency-level cl)))]
    (when if-exists
      (.withCondition delete (DeleteIfExists.)))
    (when-not (nil? condition)
      (->> (for [[column value operator] condition]
             (make-condition-exp column value operator))
           (into-array ConditionalExpression)
           DeleteIf.
           (.withCondition delete)))
    delete))

(defn prepare-put
  [{:keys [namespace table pk ck values if-exists condition cl]
    :or {cl :eventual}}]
  (let [pk     (r/make-keys pk)
        ck     (r/make-keys ck)
        col-vals (r/make-values values)
        put    (-> (Put. pk ck)
                   (.forNamespace namespace)
                   (.forTable table)
                   (.withConsistency (consistency-level cl)))]
    (when-not (nil? if-exists)
      (.withCondition put (if if-exists (PutIfExists.) (PutIfNotExists.))))
    (when-not (nil? condition)
      (->> (for [[column value operator] condition]
             (make-condition-exp column value operator))
           (into-array ConditionalExpression)
           PutIf.
           (.withCondition put)))
    (for [v col-vals]
      (.withValue put v))))
