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

(defn prepare-storage-service
  "Prepare a StorageService instance"
  [config]
  (-> config
      get-properties
      DatabaseConfig.
      StorageModule.
      vector
      Guice/createInjector
      (.getInstance StorageService)))

(def get-storage-service (memoize prepare-storage-service))

(defprotocol StorageProto
  (get! [this namespace table keys]
        [this namespace table keys cl])
  (put! [this namespace table keys values]
        [this namespace table keys values cl]))

(defrecord Storage [storage]
  StorageProto
  (get! [this namespace table keys]
    (get! this namespace table keys :eventual))

  (get! [this namespace table keys cl]
    (let [opt-result (.get (:storage this)
                           (op/prepare-get namespace table keys cl))]
      (if-let [result (.get opt-result)]
        (r/get-record result true)
        nil)))

  (put! [this namespace table keys values]
    (put! this namespace table keys values :eventual))

  (put! [this namespace table keys values cl]
    (.put (:storage this)
          (op/prepare-put namespace table keys values cl))))

(defn setup-storage [config]
  (Storage. (get-storage-service config)))
