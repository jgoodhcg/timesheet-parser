(ns timesheet-parser.core
  (:gen-class)
  (:require [clojure-csv.core :refer [parse-csv]]
            [clj-time.core :as time]
            [clj-time.coerce :as coerce]
            [clojure.string :refer [split]]
            [clojure.data.json :as json]))

(defn -main
  "given
  - an absolute path to a timesheet csv file
  return a JSON array of formatted objects"
  [& args]
  (println "Hello, World!"))

(def key-map
  {:project ["Project"]
   :tags ["Tags"]
   :description ["Description"]
   :start ["Date" "Start time"]
   :end ["Date" "End time"]})

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
        hours_meridian (Integer/parseInt
                           (first tm_split_colon))

        ;; known that all data is post y2k
        year    (+ 2000
                   (Integer/parseInt (nth d_split_slash 2)))
        month   (Integer/parseInt (nth d_split_slash 0))
        day     (Integer/parseInt (nth d_split_slash 1))
        hours   (cond
                  (and (= meridian "PM") (not= hours_meridian 12))
                  (+ 12 hours_meridian)
                  
                  (and (= meridian "AM") (= hours_meridian 12))
                  0
                  
                  :else hours_meridian)
    
        minutes (Integer/parseInt (last tm_split_colon))]

  (coerce/to-long
   (time/date-time year month day hours minutes)
   )
  ))

(defn map-map
  "
  Returns a new map with each key-value pair in `m` transformed by `f`.
  `f` takes the arguments `[key value]` and should return a value castable
  to a map entry, such as `{transformed-key transformed-value}`.
  COPY PASTA FROM http://stackoverflow.com/a/20171562/5040125
  "
  [f m]
  (into (empty m) (map #(apply f %) m)) )

(defn map-map-values
  "
  COPY PASTA FROM http://stackoverflow.com/a/20171562/5040125
  "
  [f m]
  (map-map (fn [key value] {key (f value)}) m) )

(let [csv-str
      (slurp "/timesheet-parser/resources/timesheet.csv")
      csv-vec (parse-csv csv-str)
      keys (first csv-vec)
      key-index-map (map-map-values
                     (fn [v]
                       (vec
                        (map #(.indexOf keys %) v))) key-map)]

  (->> (rest csv-vec)
       (map
        (fn [row]
          (let [start       (unix-time
                             (nth row (first (:start key-index-map)))
                             (nth row (last (:start key-index-map))))
                end         (unix-time
                             (nth row (first (:end key-index-map)))
                             (nth row (last (:end key-index-map))))
                project     (nth row (first (:project key-index-map)))
                description (nth row (first (:description key-index-map)))
                tags        (nth row (first (:tags key-index-map)))]
            ;; {:start start
            ;;  :end end
            ;;  :project project
            ;;  :description description
            ;;  :tags tags}

            (let [duration (nth row 5)
                  d_split_colon (split duration #":")
                  hours (Integer/parseInt (nth d_split_colon 0))
                  minutes (Integer/parseInt (nth d_split_colon 1))
                  seconds (Integer/parseInt (nth d_split_colon 2))
                  total_ms (+
                            (->> hours (* 60)(* 60)(* 1000))
                            (->> minutes (* 60)(* 1000))
                            (->> seconds (* 1000)))
                  ]
              (clojure.pprint/pprint
               {:start-unix start
                :end-unix end
                :is_valid (=
                           end 
                           (+ start total_ms))
                })

              )

            )))
       ;; json/write-str
       ;; (spit "/timesheet-parser/resources/timesheet.json")

       ))
