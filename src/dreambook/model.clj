(ns dreambook.model
  (:require [dreambook.dates :as d]
            [dreambook.org :as org]))

(defn parse-dreams [s]
  (let [section-bunches
        (->> s
             org/split-headers-and-body
             second
             org/convert-body-to-sections
             rest
             (remove #{"\n"})
             (partition-by (comp #{:h1} first))
             (partition 2))]
    (for [[[[_ a]] b] section-bunches
          [[_ day-str] txt] (partition 2 b)
          :let [d (d/parse-date day-str)]]
      {:year (Integer. a)
       :date d
       :id (d/format-date-as-id d)
       :txt txt})))

(defn dream-dates [dreams]
  (map :date dreams))

(defn dreams-for-year-month [year month dreams]
  (filter #(and (= year (.getYear (:date %)))
                (= month (d/month-name (.getMonth (:date %)))))
          dreams))
