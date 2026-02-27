(ns scarab.integration-config
  (:require [clojure.string :as str]))

(defn- env
  [k]
  (System/getenv k))

(defn integration-config
  "Build integration-test config from environment variables.

  Defaults to local Cassandra for backward compatibility.

  Supported vars:
  - SCARAB_TEST_STORAGE (e.g. jdbc, cassandra)
  - SCARAB_TEST_CONTACT_POINTS (e.g. jdbc:postgresql://localhost:5432/postgres)
  - SCARAB_TEST_USERNAME
  - SCARAB_TEST_PASSWORD"
  []
  (let [storage (or (env "SCARAB_TEST_STORAGE") "cassandra")
        contact-points (or (env "SCARAB_TEST_CONTACT_POINTS") "localhost")
        username (or (env "SCARAB_TEST_USERNAME") "cassandra")
        password (or (env "SCARAB_TEST_PASSWORD") "cassandra")]
    {:storage storage
     :nodes (if (str/includes? contact-points ",")
              (str/split contact-points #",")
              [contact-points])
     :username username
     :password password}))
