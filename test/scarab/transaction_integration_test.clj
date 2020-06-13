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

  CREATE TRANSACTION TABLE testks.tx2 (
      id INT PARTITIONKEY,
      ver TEXT CLUSTERINGKEY,
      val INT,
  );
  ```"
  (:require [clojure.test :refer :all]
            [scarab.transaction :as t]))

(deftest ^:integration transaction-put-select-test
  (let [pk   {:id [1 :int]}
        values {:val [111 :int]}]
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
             [111 :int])))))

(deftest ^:integration transaction-put-delete-get-test
  (let [pk {:id [2 :int]}
        values {:val [222 :int]}]
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
                           :pk pk}) nil)))))

(deftest ^:integration transaction-put-select-with-ck-test
  (let [pk   {:id [1 :int]}
        ck {:ver ["version1" :text]}
        values {:val [111 :int]}]
    (let [tx (t/start-transaction {})]
      (t/put tx {:namespace "testks"
                 :table "tx2"
                 :pk pk
                 :ck ck
                 :values values})
      (t/commit tx))

      ; start a new transaction
    (let [tx (t/start-transaction {})
          r (t/select tx {:namespace "testks"
                          :table "tx2"
                          :pk pk
                          :ck ck})]
      (is (= (:id r)
             [1 :int]))
      (is (= (:ver r)
             ["version1" :text]))
      (is (= (:val r)
             [111 :int])))))
