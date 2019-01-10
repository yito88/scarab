(ns scarab.transaction-test
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



(deftest transaction-put-get-test
  (let [tx     (t/setup-transaction {})
        keys   [{:id {:value 1 :type :int}}]
        values {:val {:value 111 :type :int}}]
    (testing "Put and get a record"
             (t/start! tx)
             (t/put! tx "testks" "tx" keys values)
             (t/commit! tx)

             ; start a new transaction
             (t/start! tx)
             (let [r (t/get! tx "testks" "tx" keys)]
               (is (= (:id r)
                      {:value 1 :type :int}))
               (is (= (:val r)
                      {:value 111 :type :int}))))))

(deftest transaction-put-delete-get-test
  (let [tx     (t/setup-transaction {})
        keys   [{:id {:value 2 :type :int}}]
        values {:val {:value 222 :type :int}}]
    (testing "Put, delete and get a record"
             (t/start! tx)
             (t/put! tx "testks" "tx" keys values)
             (t/commit! tx)

             ; start a new transaction
             (t/start! tx)
             ; read a record for the following delete
             (t/get! tx "testks" "tx" keys)
             (t/delete! tx "testks" "tx" keys)
             (t/commit! tx)

             ; start a new transaction
             (t/start! tx)
             (is (= (t/get! tx "testks" "tx" keys) nil)))))
