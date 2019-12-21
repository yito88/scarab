(ns scarab.storage
  (:require [scarab
             [core   :refer :all]
             [op     :as op]
             [record :as r]])
  (:import (com.scalar.database.api DistributedStorage
                                    Result)
           (com.scalar.database.config DatabaseConfig)
           (com.scalar.database.service StorageModule
                                        StorageService)
           (com.google.inject Guice)))

(def storage-service (atom nil))

(defprotocol StorageProto
  (select [this param])
  (delete [this param])
  (put [this param]))

(defrecord Storage [service]
  StorageProto
  (select [_ param]
    (let [opt-result (.get @service (op/prepare-get param))]
      (if (.isPresent opt-result)
        (r/get-record (.get opt-result) true)
        nil)))

  (delete [_ param]
    (.delete @service (op/prepare-delete param)))

  (put [_ param]
    (.put @service (op/prepare-put param))))

(defn- init-storage-service!
  [config]
  (when (nil? @storage-service)
    (when-let [injector (-> config
                            get-properties
                            DatabaseConfig.
                            StorageModule.
                            vector
                            Guice/createInjector)]
      (try
        (compare-and-set! storage-service
                          nil
                          (.getInstance injector StorageService))
        (catch Exception e
          (prn (.getMessages e))))))
  storage-service)

(defn prepare-storage
  "Prepare a Storage instance"
  [config]
  (->Storage (init-storage-service! config)))
