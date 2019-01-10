(ns scarab.transaction
  (:require [scarab
             [core   :refer :all]
             [op     :as op]
             [record :as r]])
  (:import (com.scalar.database.api DistributedTransaction
                                    Isolation
                                    Result
                                    TransactionState)
           (com.scalar.database.config DatabaseConfig)
           (com.scalar.database.service TransactionModule
                                        TransactionService)
           (com.google.inject Guice)))

(defn prepare-transaction-service
  "Prepare a TransactionService instance"
  [config]
  (-> config
      get-properties
      DatabaseConfig.
      TransactionModule.
      vector
      Guice/createInjector
      (.getInstance TransactionService)))

(def get-transaction-service (memoize prepare-transaction-service))

(def init-tx
  (ref nil))

(defprotocol TransactionProto
  (start! [this])
  (commit! [this])
  (get! [this namespace table keys]
        [this namespace table keys cl])
  (delete! [this namespace table keys]
           [this namespace table keys cl])
  (put! [this namespace table keys values]
        [this namespace table keys values cl]))

(defrecord Transaction [config tx]
  TransactionProto
  (start! [this]
    (dosync
      (ref-set tx (-> this :config get-transaction-service .start))))

  (commit! [this]
    (.commit (deref (:tx this))))

  (get! [this namespace table keys]
    (get! this namespace table keys :linearizable))

  (get! [this namespace table keys cl]
    (let [opt-result (.get (deref (:tx this))
                           (op/prepare-get namespace table keys cl))]
      (if (.isPresent opt-result)
        (r/get-record (.get opt-result) true)
        nil)))

  (delete! [this namespace table keys]
    (delete! this namespace table keys :linearizable))

  (delete! [this namespace table keys cl]
    (.delete (deref (:tx this)) (op/prepare-delete namespace table keys cl)))

  (put! [this namespace table keys values]
    (put! this namespace table keys values :linearizable))

  (put! [this namespace table keys values cl]
    (.put (deref (:tx this)) (op/prepare-put namespace table keys values cl))))

(defn setup-transaction [config]
  (Transaction. config init-tx))
