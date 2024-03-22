(ns dreambook.dates
  (:require [clojure.java.shell :as sh]
            [clojure.string :as str]
            [dreambook.org :as org])
  (:import [java.time LocalDate]
           [java.time.format DateTimeFormatter TextStyle]
           [java.util Locale]))

(def ^:private org-date-pattern #"(<\d{4}-\d{2}-\d{2}.+?>)")

(def ^:private fmt (DateTimeFormatter/ofPattern "<yyyy-MM-dd EEE>"))

(defn parse-date [s]
  (LocalDate/parse s fmt))

(defn- format-date-as-id [d]
  (format "%4d-%02d-%02d"
          (.getYear d)
          (.getMonthValue d)
          (.getDayOfMonth d)))

(defn format-date-for-section [d]
  (format "%s, %s %d"
          (str/capitalize (.getDayOfWeek d))
          (str/capitalize (.getMonth d))
          (.getDayOfMonth d)))

(defn- month-name [month]
  (.getDisplayName month TextStyle/FULL Locale/US))

(defn- year-months [dates]
  (->> dates
       (map (juxt #(.getYear %) (comp month-name #(.getMonth %))))
       distinct
       (partition-by first)
       (map (juxt ffirst (partial map second)))))
