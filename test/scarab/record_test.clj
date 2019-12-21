(ns scarab.record-test
  (:require [clojure.test :refer :all]
            [scarab.record :as r])
  (:import (com.scalar.database.io IntValue
                                   Key
                                   TextValue)))

(deftest make-values-test
  (let [col {:c1 [111 :int]
             :c2 ["test" :text]}
        expected [(IntValue. "c1" 111) (TextValue. "c2" "test")]]
    (testing "Make values"
      (is (= (r/make-values col) expected)))
    (testing "Check invalid values"
      (is (thrown? java.lang.AssertionError
                   (r/make-values {:c1 [111]}))))))

(deftest make-keys-test
  (let [ks {:k1 [111 :int]
            :k2 ["test" :text]}
        expected (Key. [(IntValue. "k1" 111) (TextValue. "k2" "test")])]
    (testing "Make keys"
      (is (= (r/make-keys ks) expected)))
    (testing "Check invalid values"
      (is (thrown? java.lang.AssertionError
                   (r/make-values {:k1 [:int]}))))))
