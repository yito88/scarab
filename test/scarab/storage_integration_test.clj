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

   CREATE TABLE testks.testtbl2 (
       id INT PARTITIONKEY,
       ver TEXT CLUSTERINGKEY,
       val INT,
   );
   ```"
  (:require [clojure.test :refer :all]
            [scarab.storage :as st]))

(deftest ^:integration storage-put-select-test
  (let [storage (st/prepare-storage {})
        pk {:id [1 :int]}
        values {:val [111 :int]}]
    (testing "Put and select a record"
      (st/put storage {:namespace "testks"
                       :table "testtbl"
                       :pk pk
                       :values values})
      (is (= (st/select storage {:namespace "testks"
                                 :table "testtbl"
                                 :pk pk})
             {:id [1 :int]
              :val [111 :int]})))))

(deftest ^:integration storage-put-delete-select-test
  (let [storage (st/prepare-storage {})
        pk    {:id [2 :int]}
        values  {:val [222 :int]}]
    (testing "Put, delete then select a record"
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

(deftest ^:integration storage-put-select-with-ck-test
  (let [storage (st/prepare-storage {})
        pk {:id [1 :int]}
        ck {:ver ["version1" :text]}
        values  {:val [111 :int]}]
    (testing "Put and select a record with a clustering key"
      (st/put storage {:namespace "testks"
                       :table "testtbl2"
                       :pk pk
                       :ck ck
                       :values values})
      (is (= (st/select storage {:namespace "testks"
                                 :table "testtbl2"
                                 :pk pk
                                 :ck ck})
             {:id [1 :int]
              :ver ["version1" :text]
              :val [111 :int]})))))
