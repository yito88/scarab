(defproject scarab "1.0-alpha4-SNAPSHOT"
  :description "Clojure wrapper of Scalar DB"
  :url "https://github.com/yito88/scarab"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.10.1"]
                 [com.scalar-labs/scalardb "1.2.1"]]
  :test-selectors {:default (complement :integration)
                   :integration :integration})
