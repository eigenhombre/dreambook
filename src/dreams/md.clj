(ns dreams.md
  (:require [clojure.string :as str]
            [dreams.dates :as d]))

(defn- apply-regexes [s]
  (-> s
      (str/replace #"(?<!\S)\/([^\/]+)\/(?!\S)" "*$1*")
      (str/replace #"--" "–")
      (str/replace #"<<" "«")
      (str/replace #">>" "»")
      (str/replace #"=([^=]+?)=" "`$1`")
      (str/replace #"(?m)^\* ([^*]+?\n)" "# $1")
      (str/replace #"(?m)^\*\* ([^*]+?\n)" "## $1")
      (str/replace #"(?m)^\*\*\* ([^*]+?\n)" "### $1")
      (str/replace #"(?m)^(?:#\+.+?\n)\s*" "")))

(defn- convert-dates [s]
  (str/replace s
               d/org-date-pattern
               (comp d/format-date-for-section
                     d/parse-date
                     second)))

(defn org->md
  "
  Convert Org `/s/` to MD `*s*`, etc. and
  "
  [s]
  (-> s
      apply-regexes
      convert-dates))
