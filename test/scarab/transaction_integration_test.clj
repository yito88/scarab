(ns scarab.transaction-integration-test
  "These tests are integration tests
  They need C* with keyspace 'testks' and table 'tx'

  ```transaction_test.scql
  REPLICATION FACTOR 1;
  CREATE NAMESPACE testks;

  CREATE TRANSACTION TABLE testks.tx (
      id INT PARTITIONKEY,
      val INT,
  );
  ```"
  (:require [clojure.test :refer :all]
            [scarab.transaction :as t]))

(deftest transaction-put-select-test
  (let [pk   {:id [1 :int]}
        values {:val [111 :int]}]
    (testing "Put and get a record"
      (let [tx (t/start-transaction {})]
        (t/put tx {:namespace "testks"
                   :table "tx"
                   :pk pk
                   :values values})
        (t/commit tx))

      ; start a new transaction
      (let [tx (t/start-transaction {})
            r (t/select tx {:namespace "testks"
                            :table "tx"
                            :pk pk})]
        (is (= (:id r)
               [1 :int]))
        (is (= (:val r)
               [111 :int]))))))

(deftest transaction-put-delete-get-test
  (let [pk {:id [2 :int]}
        values {:val [222 :int]}]
    (testing "Put, delete and get a record"
      (let [tx (t/start-transaction {})]
        (t/put tx {:namespace "testks"
                   :table "tx"
                   :pk pk
                   :values values})
        (t/commit tx))

      ; start a new transaction
      (let [tx (t/start-transaction {})]
        ; read a record for the following delete
        (t/select tx {:namespace "testks"
                      :table "tx"
                      :pk pk})
        (t/delete tx {:namespace "testks"
                      :table "tx"
                      :pk pk})
        (t/commit tx))

      ; start a new transaction
      (let [tx (t/start-transaction {})]
        (is (= (t/select tx {:namespace "testks"
                             :table "tx"
                             :pk pk}) nil))))))