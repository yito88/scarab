[![Clojars Project](https://img.shields.io/clojars/v/scarab.svg)](https://clojars.org/scarab)
![](https://github.com/yito88/scarab/workflows/All%20Tests/badge.svg)

# Scarab

A Clojure wrapper of [Scalar DB](https://github.com/scalar-labs/scalardb)

## Current status
- Support only `select`, `put` and `delete`

## Install

Add the following dependency to your `project.clj` file:
```clojure
[scarab "1.0-alpha3"]
```

## Usage

- You need to create a namespace and a table for Scalar DB before use
  - You can create them easily with [Schema Tools](https://github.com/scalar-labs/scalardb/tree/master/tools/schema)

### Storage (non transactional operation)

```clojure
(require '[scarab.core :as c])
(require '[scarab.storage :as st])

(let [config {:nodes "192.168.1.32,192.168.1.23"
              :username "cassandra"
              :password "cassandra"}
      storage (st/prepare-storage config)
      partition-keys {:id [1 :int] :name ["XXX" :text]}
      clustering-keys {:age [11 :int]}
      values  {:val1 [111 int] :val2 ["value 2" :text]}]

      (st/put storage {:namespace "test"
                       :table "testtbl"
                       :pk partition-keys
                       :ck clustering-keys
                       :values values})

      (st/select storage {:namespace "test"
                          :table "testtbl"
                          :pk partition-keys
                          :ck clustering-keys}))
```

- First, you need to get a storage service with properties
  - If you give an empty map as properties, it will be connected to a local server.
  ```clojure
  (st/prepare-storage {})
  ```

- You can operate records by `select`, `put` and `delete` with storage service
  ```clojure
  (st/select storage {:namespace "test"
                      :table "testtbl"
                      :pk partition-keys
                      :ck clustering-keys} ;; :ck is optional
  (st/put storage {:namespace "test"
                   :table "testtbl"
                   :pk partition-keys
                   :ck clustering-keys ;; :ck is optional
                   :values values})
  (st/delete storage {:namespace "test"
                      :table "testtbl"
                      :pk partition-keys
                      :ck clustering-keys}) ;; :ck is optional
  ```

- Columns are represented as a map
  ```clojure
    {:column-name1 {val1 :value-type1}
     :column-name2 {val2 :value-type2}}
  ```
  - `:value-type` supports `:bigint`, `:blob`, `:boolean`, `:double`, `:float`, `int`, and `text`

- Partition keys and clustering keys are represented as a map
  ```clojure
    {:key-name1 ["partition key 1" :text]
     :key-name2 [22 :int]}
  ```

- You can specify a consistency level to `select`/`put`/`delete` a record
  ```clojure
  (st/select storage {:namespace "test"
                      :table "testtbl"
                      :pk partition-keys
                      :cl :eventual}
  (st/put storage {:namespace "test"
                   :table "testtbl"
                   :pk partition-keys
                   :values values
                   :cl :sequential})
  (st/delete storage {:namespace "test"
                      :table "testtbl"
                      :pk partition-keys
                      :cl :sequential})
  ```
  - You can see the detail of consistency level in [Scalar DB](https://scalar-labs.github.io/scalardb/javadoc/com/scalar/database/api/Consistency.html)

### Transaction

```clojure
(require '[scarab.core :as c])
(require '[scarab.transaction :as t])

(let [config {:nodes "192.168.1.32,192.168.1.23,192.168.1.11"
              :username "cassandra"
              :password "cassandra"}
      namespace "testks"
      table    "tx"
      tx       (t/start-transaction config)
      partition-key {:id [1 :int]}
      values   {:val [111 :int]}]

      (t/put tx {:namespace "testks"
                 :table "tx"
                 :pk partition-keys
                 :values values})
      (t/commit tx)

      ; You have to start a new transaction after commit
      (let [tx (t/start-transaction config)
            cur-val (first (:val (t/select tx {:namespace "testks"
                                               :table "tx"
                                               :pk partition-keys})))
            new-val {:val [(+ cur-val 222) :int]}]
        ; update
        (t/put tx {:namespace "testks"
                   :table "tx"
                   :pk partition-keys
                   :values new-val})
        (t/commit tx))
```

- First, you need to set up a transaction service with properties
  ```clojure
  (t/start-transaction config)
  ```

- After operating records, you should `commit` the transaction to persist updates
  ```clojure
  (t/commit tx)
  ```

- It is the same as `storage` how to operating records
  ```clojure
  (t/select tx {:namespace "test"
                :table "testtbl"
                :pk partition-keys
                :ck clustering-keys} ;; :ck is optional
  (t/put tx {:namespace "test"
             :table "testtbl"
             :pk partition-keys
             :ck clustering-keys ;; :ck is optional
             :values values})
  (t/delete tx {:namespace "test"
                :table "testtbl"
                :pk partition-keys
                :ck clustering-keys}) ;; :ck is optional
  ```

## License

Copyright © 2019 Yuji Ito

Distributed under the [Eclipse Public License version 1.0](http://www.eclipse.org/legal/epl-v10.html) or [Apache Public License 2.0](http://www.apache.org/licenses/LICENSE-2.0.html).
