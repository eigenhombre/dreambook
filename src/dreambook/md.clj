(ns dreambook.md
  (:require [clojure.string :as str]
            [dreambook.dates :as d]))

(defn apply-regexes
  "
  Apply transformations to convert Org to MD.
  This is obviously an incomplete list, but it
  was enough for the purposes of my dream journal.
  "
  [s]
  (-> s
      (str/replace #"(?<!\S)\/([^\/]+)\/([\.”\"]*)?(?!\S)" "*$1*$2")
      (str/replace #"--" "—")
      (str/replace #"<<" "«")
      (str/replace #">>" "»")
      (str/replace #"=([^=]+?)=" "`$1`")
      (str/replace #"(?m)^\* ([^*]+?\n)" "# $1")
      (str/replace #"(?m)^\*\* ([^*]+?\n)" "## $1")
      (str/replace #"(?m)^\*\*\* ([^*]+?\n)" "### $1")
      (str/replace #"(?m)^(?:#\+.+?\n)\s*" "")))

(defn- convert-dates
  "
  Convert Org Mode-formatted dates to dates formatted e.g.,

      Wednesday, March 6, 2024

  "
  [s]
  (str/replace s
               d/org-date-pattern
               (comp d/format-date-for-section
                     d/parse-date
                     second)))

(defn org->md [s]
  (-> s
      apply-regexes
      convert-dates))
