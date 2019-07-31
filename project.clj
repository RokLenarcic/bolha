(defproject bolha "0.1.0-SNAPSHOT"
  :description "Scraper"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.10.1"]
                 [clj-http "3.9.0"]
                 [enlive "1.1.6"]
                 [org.clojure/tools.cli "0.3.5"]
                 [com.draines/postal "2.0.3"]]
  :main ^:skip-aot bolha
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
