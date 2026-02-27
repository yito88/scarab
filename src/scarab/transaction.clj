(ns scarab.transaction
  (:require [scarab
             [core   :refer :all]
             [op     :as op]
             [record :as r]])
  (:import (com.scalar.db.config DatabaseConfig)
           (com.scalar.db.service TransactionModule
                                  TransactionFactory
                                  TransactionService
                                  TwoPhaseCommitTransactionService)
           (com.google.inject Guice)))

(def transaction-service (atom nil))
(def two-phase-transaction-service (atom nil))

(defprotocol TransactionProto
  (commit [this])
  (select [this param])
  (delete [this param])
  (put [this param]))

(defprotocol TwoPhaseTransactionProto
  (id [this])
  (prepare [this])
  (validate [this])
  (commit [this])
  (rollback [this])
  (abort [this])
  (select [this param])
  (delete [this param])
  (put [this param]))

(defrecord Transaction [tx]
  TransactionProto
  (commit [_]
    (.commit tx))

  (select [_ param]
    (let [opt-result (.get tx (op/prepare-get param))]
      (if (.isPresent opt-result)
        (r/get-record (.get opt-result) true)
        nil)))

  (delete [_ param]
    (.delete tx (op/prepare-delete param)))

  (put [_ param]
    (.put tx (op/prepare-put param))))

(defrecord TwoPhaseTransaction [tx]
  TwoPhaseTransactionProto
  (id [_]
    (.getId tx))

  (prepare [_]
    (.prepare tx))

  (validate [_]
    (.validate tx))

  (commit [_]
    (.commit tx))

  (rollback [_]
    (.rollback tx))

  (abort [_]
    (.abort tx))

  (select [_ param]
    (let [opt-result (.get tx (op/prepare-get param))]
      (if (.isPresent opt-result)
        (r/get-record (.get opt-result) true)
        nil)))

  (delete [_ param]
    (.delete tx (op/prepare-delete param)))

  (put [_ param]
    (.put tx (op/prepare-put param))))

(defn- init-transaction-service!
  [config]
  (when (nil? @transaction-service)
    (when-let [injector (-> config
                            get-properties
                            DatabaseConfig.
                            TransactionModule.
                            vector
                            Guice/createInjector)]
      (try
        (compare-and-set! transaction-service
                          nil
                          (.getInstance injector TransactionService))
        (catch Exception e
          (prn (.getMessages e))))))
  transaction-service)

(defn- init-two-phase-transaction-service!
  [config]
  (when (nil? @two-phase-transaction-service)
    (try
      (let [service (-> config
                        get-properties
                        TransactionFactory/create
                        .getTwoPhaseCommitTransactionManager
                        TwoPhaseCommitTransactionService.)]
        (compare-and-set! two-phase-transaction-service nil service))
      (catch Exception e
        (prn (.getMessage e)))))
  two-phase-transaction-service)

(defn start-transaction
  "Start a transaction. Return a transaction instance"
  [config]
  (->Transaction (-> config
                     init-transaction-service!
                     deref
                     .start)))

(defn start-two-phase-transaction
  "Start a two-phase-commit transaction. Return a 2PC transaction instance."
  [config]
  (->TwoPhaseTransaction (-> config
                             init-two-phase-transaction-service!
                             deref
                             .start)))

(defn join-two-phase-transaction
  "Join an existing two-phase-commit transaction by transaction-id."
  [config transaction-id]
  (->TwoPhaseTransaction (-> config
                             init-two-phase-transaction-service!
                             deref
                             (.join transaction-id))))

(defn resume-two-phase-transaction
  "Resume an existing two-phase-commit transaction by transaction-id."
  [config transaction-id]
  (->TwoPhaseTransaction (-> config
                             init-two-phase-transaction-service!
                             deref
                             (.resume transaction-id))))
