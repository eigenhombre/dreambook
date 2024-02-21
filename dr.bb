(require '[clojure.string :as str])

(defn dreams-path [] (str (System/getenv "HOME")
                          "/org/dreams.org"))

(comment
  (def dr-test "

#+TITLE: Dreams
#+DATE: <2017-08-29 Tue>
#+OPTIONS: toc:nil num:nil

* 1998

** <1998-11-01 Sun>

[Date is approximate.]

I am about to give a talk. It will be about first guess reconstruction
methods for AMANDA.  But I also have notes from Francis. They are....

* 2000

** <2000-08-01 Tue>

Witches in a hospital (film).  A ritual – one of them says to me and
 
** <2000-08-02 Wed>

A few dreams – Looking at a world map and drinking milk with New
Zealanders.
"))

(defn split-headers-and-body [txt]
  (let [[_ & rs]
        (re-find #"(?x)
                   (\n*     # Pick up trailing newlines
                    (?:\#   # Anything starting w/ '#'
                     (?:    # Not starting with:
                      (?!\+(?:HTML:|CAPTION|BEGIN|ATTR_HTML))
                            # Swallow all lines that match
                      .)+\n*)*)
                            # Swallow everything else as group 2
                   ((?s)(?:.+)*)"
                 txt)]
    rs))

(defn vec*
  "
  Like list*, but for vectors.  (vec* :a :b [:c :d]) => [:a :b :c :d].
  "
  [& args]
  (let [l (last args)
        bl (butlast args)]
    (vec (concat bl l))))

(defn convert-body-to-sections [body]
  (let [matches
        (re-seq #"(?x)
                  (?:
                    (?:
                      (\*+)
                      \s+
                      (.+)
                      \n
                    )|
                    (
                      (?:
                        (?!\*+\s+)
                        .*\n
                      )*
                    )
                  )"
                body)]
    (->> (for [[_ stars hdr body] matches]
           (if stars
             [(-> stars
                  count
                  ((partial str "h"))
                  keyword)
              hdr]
             body))
         (remove #{""})
         (vec* :div))))

(import '[java.time LocalDate]
        '[java.time.format DateTimeFormatter]
        '[java.util Locale])

(def fmt (DateTimeFormatter/ofPattern "<yyyy-MM-dd EEE>"))

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

(defn parse-dreams [s]
  (let [year-bunches
        (->> s
             split-headers-and-body
             second
             convert-body-to-sections
             rest
             (remove #{"\n"})
             (partition-by (comp #{:h1} first))
             (partition 2))]
    (for [[[[_ a]] b] year-bunches
          [[_ day-str] txt] (partition 2 b)
          :let [d (parse-date day-str)]]
      {:year (Integer. a)
       :date d
       :id (format-date-as-id d)
       :txt txt})))

(defn toc-str [dreams]
  (let [years (->> dreams
                   (map :year)
                   distinct)]
    (str/join
     "\n"
     (concat
      ["Contents"]
      (for [yr years
            :let [dream-years (filter (comp #{yr} #(.getYear %) :date)
                                      dreams)]]
        (str/join "\n" (concat [(str "- " yr)]
                               (for [dr dream-years]
                                 (format "    - [%s](#%s)"
                                         (format-date (:date dr))
                                         (:id dr))))))))))

(comment
  ;; Years
  (->> (dreams-path)
       slurp
       parse-dreams
       (map :year)
       distinct)
  ;;=>
  (1998 2000 2002 2011 2017 2019 2021 2022 2023 2024)

  ;; Days
  (->> (dreams-path)
       slurp
       parse-dreams
       (map :date)
       count)
  ;;=>
  148

  ;; Everything:
  (->> (dreams-path)
       slurp
       parse-dreams
       (map :id))
  ;;=>
  ("1998-11-01" "2000-08-01" "2000-08-02" "2000-08-03" "2000-08-05"
   "2000-08-06" "2000-08-08" "2000-08-09" "2000-08-11" "2000-08-29"
   "2000-08-31" "2002-02-22" "2002-03-04" "2002-03-05" "2002-03-06"
   "2002-03-08" "2002-03-11" "2002-03-12" "2002-03-13" "2002-03-18"
   "2002-03-19" "2002-03-20" "2002-03-22" "2002-03-23" "2002-03-25"
   "2002-03-26" "2002-03-27" "2002-03-28" "2011-04-09" "2011-04-10"
   "2011-04-11" "2011-04-26" "2017-08-29" "2017-09-17" "2019-08-05"
   "2019-08-07" "2019-08-08" "2019-08-11" "2019-08-12" "2019-08-15"
   "2019-08-16" "2019-08-23" "2019-08-24" "2019-08-25" "2019-08-26"
   "2019-08-27" "2019-08-28" "2019-08-29" "2019-08-30" "2019-08-31"
   "2019-09-01" "2019-09-02" "2019-09-04" "2019-09-05" "2019-09-07"
   "2019-09-08" "2019-09-11" "2019-09-12" "2019-09-13" "2019-09-14"
   "2019-09-15" "2019-09-16" "2019-09-17" "2019-09-18" "2019-09-21"
   "2019-09-22" "2019-09-26" "2019-09-28" "2019-10-01" "2019-10-02"
   "2019-10-06" "2019-10-07" "2019-10-09" "2019-10-10" "2019-10-11"
   "2019-10-12" "2019-10-13" "2019-10-14" "2019-10-16" "2019-10-17"
   "2019-10-18" "2019-10-22" "2019-10-23" "2019-10-24" "2019-10-25"
   "2019-10-28" "2019-10-30" "2019-11-03" "2019-11-06" "2019-11-07"
   "2019-11-09" "2019-11-15" "2019-11-16" "2019-11-29" "2019-12-02"
   "2019-12-03" "2019-12-04" "2019-12-08" "2019-12-11" "2019-12-12"
   "2019-12-13" "2019-12-22" "2021-04-18" "2021-05-02" "2022-02-01"
   "2022-02-08" "2022-02-12" "2022-01-22" "2022-01-21" "2022-01-24"
   "2022-01-31" "2022-02-20" "2022-05-02" "2022-05-17" "2022-05-23"
   "2022-05-25" "2022-05-27" "2022-08-19" "2022-09-16" "2022-10-27"
   "2022-12-14" "2022-12-22" "2022-12-25" "2022-12-27" "2023-01-02"
   "2023-01-11" "2023-01-12" "2023-01-13" "2023-01-17" "2023-01-26"
   "2023-01-19" "2023-01-20" "2023-01-28" "2023-01-29" "2023-02-06"
   "2023-02-12" "2023-02-22" "2023-12-09" "2024-01-14" "2024-02-12"
   "2024-02-13" "2024-02-14" "2024-02-16" "2024-02-17" "2024-02-18"
   "2024-02-19" "2024-02-20" "2024-02-21")


  ;; TOC
  (println
   (->> (dreams-path)
        slurp
        parse-dreams
        toc-str))
  )

