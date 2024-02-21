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
          [[_ day-str] txt] (partition 2 b)]
      {:year (Integer. a)
       :day (parse-date day-str)
       :txt txt})))

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
       (map :day)
       count)
  ;;=>
  147

  ;; Everything:
  (->> (dreams-path)
       slurp
       parse-dreams)


  )
