(ns scarab.record
  (:require [scarab.core])
  (:import (com.scalar.database.io BigIntValue
                                   BlobValue
                                   BooleanValue
                                   DoubleValue
                                   FloatValue
                                   IntValue
                                   TextValue
                                   Key)))

(def types
  {:bigint  (fn [n v] (BigIntValue. n v))
   :blob    (fn [n v] (BlobValue. n v))
   :boolean (fn [n v] (BooleanValue. n v))
   :double  (fn [n v] (DoubleValue. n v))
   :float   (fn [n v] (FloatValue. n v))
   :int     (fn [n v] (IntValue. n v))
   :text    (fn [n v] (TextValue. n v))})

(defn make-value
  [c v]
  (let [name# (name c)
        {:keys [value type]} v]
    ((get types type) name# value)))

(defn make-values
  "convert to a values' vector from {:c1 {:value v1 :type :t1} :c2 {:value v2 :type :t2}} format"
  [values]
  (persistent!
    (reduce
      (fn [t [c v]]
        (conj! t (make-value c v)))
      (transient []) values)))

(defn make-key-value
  "[:name {:value value :type :type}]"
  [[k v]]
  (let [name# (name k)
        {:keys [value type]} v]
    ((get types type) name# value)))

(defn make-keys
  [keys]
  (Key. (map make-key-value keys)))

(defn get-value
  [v]
  (if (instance? TextValue v)
    (let [opt-string (.getString v)]
      (if (.isPresent opt-string)
        (.get opt-string)
        nil))
    (.get v)))

(defn get-type
  [v]
  (cond
    (instance? BigIntValue v)  :bigint
    (instance? BlobValue v)    :blob
    (instance? BooleanValue v) :boolean
    (instance? DoubleValue v)  :double
    (instance? FloatValue v)   :float
    (instance? IntValue v)     :int
    (instance? TextValue v)    :text
    :else :unknown))

(defn get-record
  ([result]
   (get-record result false))

  ([result with-type]
   (->> result
        .getValues
        (reduce
          (fn [t [k v]]
            (assoc! t
                    (keyword k)
                    (if with-type
                      {:value (get-value v) :type (get-type v)}
                      (get-value v))))
          (transient {}))
        persistent!)))
