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

(defn unix-time
  "
  turns two strings (in a vec) into a unix timestamp
  example strings '1/30/17' '9:45 AM'
  date 'm/d/yy'
  start time 'h:mm AM'
  "

  [date time_meridian]
  (let [d_split_slash (split date #"/")
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
          (let [start   (unix-time (nth row 2) (nth row 3))
                end     (unix-time (nth row 2) (nth row 4))
                project     (nth row 10)
                description (nth row 12)
                tags        (nth row 18)
                ]
            {:start start
             :end end
             :project project
             :description description
             :tags tags})))))
