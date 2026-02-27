(ns scarab.storage-integration-test
  "These tests are integration tests
   They need C* with keyspace 'testks' and table 'testtbl'

   ```storage_test.scql
   REPLICATION FACTOR 1;
   CREATE NAMESPACE testks;

   CREATE TABLE testks.testtbl (
       id INT PARTITIONKEY,
       name TEXT,
       score INT,
   );

   CREATE TABLE testks.testtbl2 (
       id INT PARTITIONKEY,
       ver INT CLUSTERINGKEY,
       val INT,
   );
  ```"
  (:require [clojure.test :refer :all]
            [scarab.integration-config :as ic]
            [scarab.storage :as st]))

(def ^:private config (ic/integration-config))

(deftest ^:integration storage-put-select-test
  (let [storage (st/prepare-storage config)
        pk {:id [1 :int]}
        values {:name ["ito" :text]
                :score [10 :int]}]
    (st/put storage {:namespace "testks"
                     :table "testtbl"
                     :pk pk
                     :values values})
    (is (= (st/select storage {:namespace "testks"
                               :table "testtbl"
                               :pk pk})
           {:id [1 :int] :name ["ito" :text] :score [10 :int]}))))

(deftest ^:integration storage-update-delete-test
  (let [storage (st/prepare-storage config)
        pk    {:id [2 :int]}]
    (st/put storage {:namespace "testks"
                     :table "testtbl"
                     :pk pk
                     :values {:name ["ito" :text] :score [10 :int]}
                     :if-exists false})
    (is (= (st/select storage {:namespace "testks"
                               :table "testtbl"
                               :pk pk})
           {:id [2 :int] :name ["ito" :text] :score [10 :int]}))

    (st/put storage {:namespace "testks"
                     :table "testtbl"
                     :pk pk
                     :values {:score [11 :int]}
                     :if-exists true})
    (is (= (st/select storage {:namespace "testks"
                               :table "testtbl"
                               :pk pk})
           {:id [2 :int] :name ["ito" :text] :score [11 :int]}))

    (st/delete storage {:namespace "testks"
                        :table "testtbl"
                        :pk pk
                        :if-exists true})
    (is (= (st/select storage {:namespace "testks"
                               :table "testtbl"
                               :pk pk})
           nil))))

(deftest ^:integration storage-scan-test
  (let [storage (st/prepare-storage config)]
    (doseq [ver (range 0 10)]
      (st/put storage {:namespace "testks"
                       :table "testtbl2"
                       :pk {:id [1 :int]}
                       :ck {:ver [ver :int]}
                       :values {:val [111 :int]}}))
    (is (= (st/scan storage {:namespace "testks"
                             :table "testtbl2"
                             :pk {:id [1 :int]}
                             :start-ck {:ver [3 :int]}
                             :inclusive-start? true
                             :end-ck {:ver [8 :int]}
                             :inclusive-end? false
                             :ordering {:ver :desc}
                             :limit 3})
           '({:id [1 :int] :ver [7 :int] :val [111 :int]}
             {:id [1 :int] :ver [6 :int] :val [111 :int]}
             {:id [1 :int] :ver [5 :int] :val [111 :int]})))))
