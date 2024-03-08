(ns dreams.parse
  (:require [dreams.dates :as d]
            [dreams.org :as org]))

(defn parse-dreams [s]
  (let [year-bunches
        (->> s
             org/split-headers-and-body
             second
             org/convert-body-to-sections
             rest
             (remove #{"\n"})
             (partition-by (comp #{:h1} first))
             (partition 2))]
    (for [[[[_ a]] b] year-bunches
          [[_ day-str] txt] (partition 2 b)
          :let [d (d/parse-date day-str)]]
      {:year (Integer. a)
       :date d
       :id (d/format-date-as-id d)
       :txt txt})))
