(ns scarab.core-test
  (:require [clojure.test :refer :all]
            [scarab.core :refer :all]))

(deftest create-properties-test-with-empty-map
  (testing "Create properties with a empty map"
    (let [prop (create-properties {})]
      (is (= (.getProperty prop "scalar.database.contact_points")
             "localhost"))
      (is (= (.getProperty prop "scalar.database.username")
             "cassandra"))
      (is (= (.getProperty prop "scalar.database.password")
             "cassandra")))))

(deftest create-properties-test
  (let [config {:nodes ["192.168.1.30" "192.168.1.31" "192.168.1.32"]
                :username "admin"
                :password "abcde1234"}]
    (testing "Create properties with a config map"
      (let [prop (create-properties config)]
        (is (= (.getProperty prop "scalar.database.contact_points")
               "192.168.1.30,192.168.1.31,192.168.1.32"))
        (is (= (.getProperty prop "scalar.database.username")
               "admin"))
        (is (= (.getProperty prop "scalar.database.password")
               "abcde1234"))))))
