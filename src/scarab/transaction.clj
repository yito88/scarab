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

(def transaction-service (atom nil))

(defprotocol TransactionProto
  (commit [this])
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

(defn start-transaction
  "Start a transaction. Return a transaction instance"
  [config]
  (->Transaction (-> config
                     init-transaction-service!
                     deref
                     .start)))
