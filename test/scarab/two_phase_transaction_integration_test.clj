(ns scarab.two-phase-transaction-integration-test
  (:require [clojure.test :refer :all]
            [scarab.integration-config :as ic]
            [scarab.transaction :as t]))

(def ^:private config (ic/integration-config))

(deftest ^:integration two-phase-transaction-put-commit-test
  (let [pk {:id [101 :int]}
        values {:val [999 :int]}
        tx (t/start-two-phase-transaction config)]
    (t/put tx {:namespace "testks"
               :table "tx"
               :pk pk
               :values values})
    (t/prepare tx)
    (t/validate tx)
    (t/commit tx)

    (let [read-tx (t/start-transaction config)]
      (is (= (t/select read-tx {:namespace "testks"
                                :table "tx"
                                :pk pk})
             {:id [101 :int] :val [999 :int]})))))

(deftest ^:integration two-phase-transaction-join-test
  (let [pk1 {:id [102 :int]}
        pk2 {:id [103 :int]}
        tx (t/start-two-phase-transaction config)
        joined (t/join-two-phase-transaction config (t/id tx))]
    (t/put tx {:namespace "testks"
               :table "tx"
               :pk pk1
               :values {:val [1001 :int]}})
    (t/put joined {:namespace "testks"
                   :table "tx"
                   :pk pk2
                   :values {:val [1002 :int]}})
    (t/prepare tx)
    (t/validate tx)
    (t/commit tx)

    (let [read-tx (t/start-transaction config)]
      (is (= (t/select read-tx {:namespace "testks"
                                :table "tx"
                                :pk pk1})
             {:id [102 :int] :val [1001 :int]}))
      (is (= (t/select read-tx {:namespace "testks"
                                :table "tx"
                                :pk pk2})
             {:id [103 :int] :val [1002 :int]})))))
