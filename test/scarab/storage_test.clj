(ns scarab.storage-test
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
  (let [storage (st/setup-storage {})
        keys    [{:id {:value 1 :type :int}}]
        values  {:val {:value 111 :type :int}}]
    (testing "Put and get a record"
             (st/put! storage "testks" "testtbl" keys values)
             (is (= (st/get! storage "testks" "testtbl" keys)
                    {:id {:value 1 :type :int}
                     :val {:value 111 :type :int}})))))

(deftest storage-put-delete-get-test
  (let [storage (st/setup-storage {})
        keys    [{:id {:value 2 :type :int}}]
        values  {:val {:value 222 :type :int}}]
    (testing "Put, delete then get a record"
             (st/put! storage "testks" "testtbl" keys values)
             (st/delete! storage "testks" "testtbl" keys)
             (is (= (st/get! storage "testks" "testtbl" keys)
                    nil)))))
