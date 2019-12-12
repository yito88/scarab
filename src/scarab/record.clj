(ns scarab.record
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

(defn valid-value?
  [value]
  (and (= (count value) 2) (keyword? (second value))))

(defn make-value
  [column value]
  (let [name# (name column)
        [v t] value]
    ((t types) name# v)))

(defn make-values
  "convert to a values' vector from {:c1 [v1 :type1] :c2 [v2 :type2]} format"
  [values]
  (persistent!
   (reduce
    (fn [t [c v]]
      (conj! t (make-value c v)))
    (transient []) values)))

(defn make-key-value
  "[:name [value :type]]"
  [[k value]]
  (let [name# (name k)
        [v t] value]
    ((t types) name# v)))

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
                     [(get-value v) (get-type v)]
                     (get-value v))))
         (transient {}))
        persistent!)))
