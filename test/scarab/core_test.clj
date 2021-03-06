(ns scarab.core-test
  (:require [clojure.test :refer :all]
            [scarab.core :refer :all]))

(deftest create-properties-test-with-empty-map
  (let [prop (create-properties {})]
    (is (= (.getProperty prop "scalar.db.contact_points")
           "localhost"))
    (is (= (.getProperty prop "scalar.db.username")
           "cassandra"))
    (is (= (.getProperty prop "scalar.db.password")
           "cassandra"))))

(deftest create-properties-test
  (let [config {:nodes ["192.168.1.30" "192.168.1.31" "192.168.1.32"]
                :username "admin"
                :password "abcde1234"}]
    (let [prop (create-properties config)]
      (is (= (.getProperty prop "scalar.db.contact_points")
             "192.168.1.30,192.168.1.31,192.168.1.32"))
      (is (= (.getProperty prop "scalar.db.username")
             "admin"))
      (is (= (.getProperty prop "scalar.db.password")
             "abcde1234")))))
