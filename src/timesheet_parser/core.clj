(ns timesheet-parser.core
  (:gen-class)
  (:require [clojure-csv.core :refer [parse-csv]]
            [clj-time.core :as time]
            [clj-time.coerce :as coerce]
            [clojure.string :refer [split]]))

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (println "Hello, World!"))

;; (def key-map {
;;               :project
;;               :tags
;;               :description
;;               :start
;;               :end
;;               })

(defn start-unix
  "
  turns two strings (in a vec) into a unix timestamp
  example strings '1/30/17' '9:45 AM'
  date {m/d/yy} at index 2
  start time {h:mm AM} at index 3
  "

  [row]
  (let [date (nth row 2)
        time_meridian (nth row 3)

        d_split_slash (split date #"/")
        tm_split_space (split time_meridian #"\s")
        tm_split_colon (split (first tm_split_space) #":")
        meridian (last tm_split_space)
        hours_meridian (- (Integer/parseInt
                           (first tm_split_colon))
                          1)

        ;; known that all data is post y2k
        year    (+ 2000
                   (Integer/parseInt (nth d_split_slash 2)))
        month   (Integer/parseInt (nth d_split_slash 0))
        day     (Integer/parseInt (nth d_split_slash 1))
        hours   (if (= meridian "PM")
                  (+ 12 hours_meridian)
                  hours_meridian)
        minutes (Integer/parseInt (last tm_split_colon))]

  (coerce/to-long
    (time/date-time year month day hours minutes))))

;; (-> "/timesheet-parser/resources/timesheet.csv"
;;     slurp
;;     parse-csv
;;     get-keys)

(let [csv-str
      (slurp "/timesheet-parser/resources/timesheet.csv")
      csv-vec (parse-csv csv-str)
      keys (first csv-vec)]

  (->> (rest csv-vec)
       (map
        (fn [row]
          (let [start (start-unix row)]
            start)))))
