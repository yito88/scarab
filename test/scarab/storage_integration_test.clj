(ns scarab.storage-integration-test
  "These tests are integration tests
   They need C* with keyspace 'testks' and table 'testtbl'

   ```storage_test.scql
   REPLICATION FACTOR 1;
   CREATE NAMESPACE testks;

   CREATE TABLE testks.testtbl (
       id INT PARTITIONKEY,
       val INT,
   );
   ```"
  (:require [clojure.test :refer :all]
            [scarab.storage :as st]))

(deftest storage-put-get-test
  (let [storage (st/prepare-storage {})
        pk    {:id [1 :int]}
        values  {:val [111 :int]}]
    (testing "Put and get a record"
      (st/put storage {:namespace "testks"
                       :table "testtbl"
                       :pk pk
                       :values values})
      (is (= (st/select storage {:namespace "testks"
                                 :table "testtbl"
                                 :pk pk})
             {:id [1 :int]
              :val [111 :int]})))))

(deftest storage-put-delete-get-test
  (let [storage (st/prepare-storage {})
        pk    {:id [2 :int]}
        values  {:val [222 :int]}]
    (testing "Put, delete then get a record"
      (st/put storage {:namespace "testks"
                       :table "testtbl"
                       :pk pk
                       :values values})
      (st/delete storage {:namespace "testks"
                          :table "testtbl"
                          :pk pk})
      (is (= (st/select storage {:namespace "testks"
                                 :table "testtbl"
                                 :pk pk})
             nil)))))
