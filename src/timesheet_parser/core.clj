(ns timesheet-parser.core
  (:gen-class)
  (:require [clojure-csv.core :refer [parse-csv]]))

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (println "Hello, World!"))

(-> "/timesheet-parser/resources/timesheet.csv"
    slurp
    parse-csv)
