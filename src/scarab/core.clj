(ns scarab.core
  (:import (java.util Properties)))

(defn create-properties
  "Make properties from a config map
    {:nodes ['192.168.1.30' '192.168.1.31' '192.168.1.32']
     :username 'admin'
     :password 'abcde1234'}"
  [config]
  (let [props (Properties.)]
    (->> (if-let [nodes (:nodes config)]
           (reduce #(str %1 "," %2) nodes)
           "localhost")
         (.setProperty props "scalar.db.contact_points"))
    (->> (if-let [username (:username config)]
           username
           "cassandra")
         (.setProperty props "scalar.db.username"))
    (->> (if-let [password (:password config)]
           password
           "cassandra")
         (.setProperty props "scalar.db.password"))
    props))

(def get-properties (memoize create-properties))
