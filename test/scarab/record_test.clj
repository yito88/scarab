(ns scarab.record-test
  (:require [clojure.test :refer :all]
            [scarab.record :as r])
  (:import (com.scalar.db.io IntValue
                             Key
                             TextValue)))

(deftest make-values-test
  (let [col {:c1 [111 :int]
             :c2 ["test" :text]}
        expected [(IntValue. "c1" 111) (TextValue. "c2" "test")]]
    (is (= expected (r/make-values col)))
    (is (thrown? java.lang.AssertionError (r/make-values {:c1 [111]})))))

(deftest make-keys-test
  (let [ks {:k1 [111 :int] :k2 ["test" :text]}
        expected (Key. [(IntValue. "k1" 111) (TextValue. "k2" "test")])]
    (is (= expected (r/make-keys ks)))
    (is (= nil (r/make-keys nil)))
    (is (thrown? java.lang.AssertionError (r/make-values {:k1 [:int]})))))
