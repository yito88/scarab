(defproject scarab "1.0.1-SNAPSHOT"
  :description "Clojure wrapper of Scalar DB"
  :url "https://github.com/yito88/scarab"
  :license {:name "Apache License 2.0"
            :url "http://www.apache.org/licenses/LICENSE-2.0"}
  :dependencies [[org.clojure/clojure "1.10.1"]
                 [com.scalar-labs/scalardb "3.17.1"]]
  :test-selectors {:default (complement :integration)
                   :integration :integration})
