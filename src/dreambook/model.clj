(ns dreambook.model
  (:require [dreambook.dates :as d]
            [dreambook.org :as org]))

(defn- maybe-parse-date [s]
  (when (d/is-date s)
    (d/parse-date s)))

(defn get-year-content-chunks [s]
  (let [year-headed-sections
        (->> s
             ;; Get rid of Org preamble:
             (org/split-headers-and-body)
             second
             ;; Find headers and their content:
             (org/convert-body-to-sections)
             rest
             (remove #{"\n"})
             ;; Split it up by year:
             (partition-by (comp #{:h1} first))
             (partition 2))]
    (for [[[[_ a]] content] year-headed-sections]
      {:year (Integer. a)
       :content content})))

(defn parse-dreams [s]
  (let [year-content-chunks (get-year-content-chunks s)]
    (for [{:keys [year content]} year-content-chunks
          [[_ day-str] body] (partition 2 content)]
      {:year year
       :date (maybe-parse-date day-str)
       :body body})))

(defn dream-dates [dreams]
  (map :date dreams))

(defn dreams-for-year-month [year month dreams]
  (filter #(and (= year (.getYear (:date %)))
                (= month (d/month-name (.getMonth (:date %)))))
          dreams))
