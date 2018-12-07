(ns scarab.record-test
  (:require [clojure.test :refer :all]
            [scarab
             [core :refer :all]
             [record :as r]])
  (:import (com.scalar.database.io IntValue
                                   Key
                                   TextValue)))

(deftest make-values-test
  (let [col {:c1 {:value 111 :type :int}
             :c2 {:value "test" :type :text}}
        expected [(IntValue. "c1" 111) (TextValue. "c2" "test")]]
    (testing "Make values"
             (is (= (r/make-values col) expected)))))

(deftest make-keys-test
  (let [ks {:k1 {:value 111 :type :int}
            :k2 {:value "test" :type :text}}
        expected (Key. [(IntValue. "k1" 111) (TextValue. "k2" "test")])]
    (testing "Make keys"
             (is (= (r/make-keys ks) expected)))))
