(ns dreams.dates
  (:require [clojure.java.shell :as sh]
            [clojure.string :as str]
            [dreams.org :as org]))

(import '[java.time LocalDate]
        '[java.time.format DateTimeFormatter TextStyle]
        '[java.util Locale])

(def ^:private fmt (DateTimeFormatter/ofPattern "<yyyy-MM-dd EEE>"))

(defn parse-date [s]
  (LocalDate/parse s fmt))

(defn format-date-as-id [d]
  (format "%4d-%02d-%02d"
          (.getYear d)
          (.getMonthValue d)
          (.getDayOfMonth d)))

(defn format-date [d]
  (format "%d/%d/%s"
          (.getMonthValue d)
          (.getDayOfMonth d)
          (.substring (str (.getYear d)) 2)))

(defn format-date-for-section [d]
  (format "%s, %s %d, %d"
          (str/capitalize (.getDayOfWeek d))
          (str/capitalize (.getMonth d))
          (.getDayOfMonth d)
          (.getYear d)))

(defn month-name [month]
  (.getDisplayName month TextStyle/FULL Locale/US))

(defn year-months [date]
  [(-> date .getYear) (month-name date)])
