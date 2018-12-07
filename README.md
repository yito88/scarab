# Scarab

A Clojure wrapper of [Scalar DB](https://github.com/scalar-labs/scalardb)

## Usage

### Storage (non transactional operation)

```clojure
(require '[scarab.core :as c])
(require '[scarab.storage :as st])

(let [properties (c/create-properties {:nodes "192.168.1.32,192.168.1.23"
                                       :username "cassandra"
                                       :password "cassandra"})
      storage (st/setup-storage properties)
      keys    [{:id {:value 1 :type :int}}]
      values  {:val {:value 111 :type :int}}]

      (st/put! storage "keyspace" "table" keys values)

      (st/get! storage "keyspace" "table" keys))
```

- Columns are represented as a map
  ```clojure
    {:column-name1 {:value val1 :type :value-type1}
     :column-name2 {:value val2 :type :value-type2}}
  ```
  - `:value-type` supports `:bigint`, `:blob`, `:boolean`, `:double`, `:float`, `int`, and `text`

- `keys` which has partition keys and clustering keys are represented as a vector of maps as below
  ```clojure
    [{:pk1 {:value "partition key 1" :type :text}
      :pk2 {:value "partition key 2" :type :text}}
     {:ck1 {:value "clustering key 1" :type :text}
      :ck2 {:value "22" :type :int}}])
  ```
  - The first example has only a partition key

### Transaction

```clojure
(require '[scarab.core :as c])
(require '[scarab.transaction :as t])

(let [properties (c/create-properties {:nodes "192.168.1.32,192.168.1.23,192.168.1.11"
                                       :username "cassandra"
                                       :password "cassandra"})
      keyspace "testks"
      table    "tx"
      tx       (t/setup-transaction properties)
      keys     [{:id {:value 1 :type :int}}]
      values   {:val {:value 111 :type :int}}]

      (t/start! tx)
      (t/put! tx keyspace table keys values)
      (t/commit! tx)

      ; You have to start a new transaction after commit
      (t/start! tx)
      (let [cur-val (:value (:val (t/get! tx keyspace table keys)))
            new-val {:val {:value (+ cur-val 222) :type :int}}]
        ; update
        (t/put! tx keyspace table keys new-val))
      (t/commit! tx))
```

- You need to create a keyspace and a table before execution
  - You can create them easily with [Schema Tools](https://github.com/scalar-labs/scalardb/tree/master/tools/schema)


## License

Copyright © 2018 Yuji Ito

Distributed under the [Eclipse Public License version 1.0](http://www.eclipse.org/legal/epl-v10.html) or [Apache Public License 2.0](http://www.apache.org/licenses/LICENSE-2.0.html).
