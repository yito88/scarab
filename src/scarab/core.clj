(ns scarab.core
  (:require [clojure.string :as str])
  (:import (java.util Properties)))

(defn- contact-points
  [config]
  (cond
    (:contact-points config) (:contact-points config)
    (string? (:nodes config)) (:nodes config)
    (seq (:nodes config)) (str/join "," (:nodes config))
    :else "localhost"))

(defn create-properties
  "Make properties from a config map
    {:nodes ['192.168.1.30' '192.168.1.31' '192.168.1.32']
     :username 'admin'
     :password 'abcde1234'}"
  [config]
  (let [props (Properties.)]
    (.setProperty props "scalar.db.contact_points" (contact-points config))
    (->> (if-let [username (:username config)]
           username
           "cassandra")
         (.setProperty props "scalar.db.username"))
    (->> (if-let [password (:password config)]
           password
           "cassandra")
         (.setProperty props "scalar.db.password"))
    (when-let [storage (:storage config)]
      (.setProperty props "scalar.db.storage" storage))
    (doseq [[k v] (:properties config)]
      (.setProperty props (name k) (str v)))
    props))

(def get-properties (memoize create-properties))
