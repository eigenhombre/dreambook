(ns dreams.core
  (:require
  [clojure.string :as str]
  [clojure.java.shell :as sh]))

(import '[java.time LocalDate]
        '[java.time.format DateTimeFormatter TextStyle]
        '[java.util Locale])

(def org-dir (str (System/getenv "HOME") "/org"))
(def dreams-path (str org-dir "/dreams.org"))
(def md-path (str org-dir "/dreams.md"))
(def cover-image (str org-dir "/dreams-cover.png"))
(def epub-output (str (System/getenv "HOME") "/Desktop/dreams.epub"))

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

(defn month-name [month]
  (.getDisplayName month TextStyle/FULL Locale/US))

(defn year-months [date]
  [(-> date .getYear) (month-name date)])

(defn dream-years [dreams]
  (->> dreams
       (map :year)
       distinct))

(defn print-years []
  (let [year-counts (->> dreams-path
                         slurp
                         parse-dreams
                         (group-by :year)
                         (sort-by first)
                         (map (juxt first (comp count second))))]
    (doseq [[yr cnt] year-counts]
      (println (format "%d: %3d%s"
                       yr
                       cnt
                       (str/join "" (repeat cnt ".")))))))

(defn wrap-n-columns [n s]
  (str/join
   "\n"
   (map #(str/join " " %)
        (partition n (str/split s #"\s+")))))

(defn dream-dates [dreams]
  (map :date dreams))

(defn print-dates []
  (let [dates (->> dreams-path
                   slurp
                   parse-dreams
                   (map :date))
        dates-str (str/join " " (map format-date dates))]
    (println (wrap-n-columns 6 dates-str))))

(defn dreams-for-year [year dreams]
  (filter (comp #{year} #(.getYear %) :date)
          dreams))

(defn dreams-for-year-month [year month dreams]
  (filter #(and (= year (.getYear (:date %)))
                (= month (month-name (.getMonth (:date %)))))
          dreams))

(defn year-months [dates]
  (->> dates
       (map (juxt #(.getYear %) (comp month-name #(.getMonth %))))
       distinct
       (partition-by first)
       (map (juxt ffirst (partial map second)))))

(defn toc-str [dreams]
  (let [ym (year-months (dream-dates dreams))]
    (str
      (str/join
      "\n"
      (for [[yr months] ym]
        (str/join "\n" (concat [(format "- [%s](#%s)" yr yr)]
                                (for [m months]
                                  (format "    - [%s](#%s-%s)"
                                          m yr m)))))))))

(defn nopunct [s]
  (str/replace s #"[,-\.\?\\\[\]\(\)\-–\"“”$’]*" "" ))

(defn normalize [w]
  (->> w
       str/lower-case
       nopunct))

(defn dreamwords []
  (let [dreams (->> dreams-path
                    slurp
                    parse-dreams)
        tokens (->> dreams
                    (map :txt)
                    (mapcat #(str/split % #"\s+|\-"))
                    (map normalize))]
    (println (->> tokens
                  frequencies
                  (sort-by second)
                  reverse
                  (map first)
                  (take 300)
                  (str/join " ")
                  (wrap-n-columns 10)))))

(defn format-date-for-section [d]
  (format "%s, %s %d, %d"
          (str/capitalize (.getDayOfWeek d))
          (str/capitalize (.getMonth d))
          (.getDayOfMonth d)
          (.getYear d)))

(defn format-dream [dream]
  (str/join
   "\n"
   [(format "# %s\n\n" (format-date-for-section (:date dream)))
    (:txt dream)]))

(defn format-dreams [dreams]
  (let [yrs-mos (year-months (dream-dates dreams))]
    (str/join
     "\n\n"
     (for [[yr mos] yrs-mos]
       (str (format "# %s\n\n" yr)
            (str/join
             "\n"
             (for [mo mos]
                (str (format "## <a name=\"%s-%s\"></a>\n\n"
                             yr mo)
                     (str/join "\n"
                               (for [{:keys [date id txt]} (dreams-for-year-month yr mo dreams)]
                                 (format "### %s\n\n%s"
                                          (format-date-for-section date)
                                          txt)))))))))))

(defn dreams-as-md [dreams]
  (let [toc (toc-str dreams)]
    (str toc "\n\n" (format-dreams dreams))))

(defn num-dreams []
  (->> dreams-path
       slurp
       parse-dreams
       count))

(defn random-dream-str []
  (let [dreams (->> dreams-path
                    slurp
                    parse-dreams)]
    (format-dream (rand-nth dreams))))

(defn org->md []
  (let [dreams (->> dreams-path
                    slurp
                    parse-dreams)]
    (spit md-path (dreams-as-md dreams))))

(defn md->epub []
  (let [{:keys [exit out err]}
        (sh/sh "ebook-convert"
               md-path
               epub-output
               "--output-profile=tablet"
               (str "--cover=" cover-image)
               "--authors=Eig N. Hombre"
               "--title=eBook of Dreams")]
    (assert (zero? exit) (str "Error: " err))
    (println out)))

(defn install-book []
  (println (sh/sh "open" "-a" "/Applications/calibre.app" epub-output))
  (println (sh/sh "open" epub-output)))
