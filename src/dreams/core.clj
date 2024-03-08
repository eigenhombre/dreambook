(ns dreams.core
  (:require
  [clojure.string :as str]
  [dreams.org :as org]
  [dreams.dates :as d]
  [clojure.java.shell :as sh]))

(import '[java.time LocalDate]
        '[java.time.format DateTimeFormatter TextStyle]
        '[java.util Locale])

(def org-dir (str (System/getenv "HOME") "/org"))
(def dreams-path (str org-dir "/dreams.org"))
(def md-path (str org-dir "/dreams.md"))
(def cover-image (str org-dir "/dreams-cover.png"))
(def epub-output (str (System/getenv "HOME") "/Desktop/dreams.epub"))


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
        dates-str (str/join " " (map d/format-date dates))]
    (println (wrap-n-columns 6 dates-str))))

(defn dreams-for-year [year dreams]
  (filter (comp #{year} #(.getYear %) :date)
          dreams))

(defn dreams-for-year-month [year month dreams]
  (filter #(and (= year (.getYear (:date %)))
                (= month (d/month-name (.getMonth (:date %)))))
          dreams))

(defn year-months [dates]
  (->> dates
       (map (juxt #(.getYear %) (comp d/month-name #(.getMonth %))))
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

(defn org->md [s]
  ;; Convert Org `/s/` to MD `*s*`:
  (-> s
      (str/replace #"/([^/]+?)/" "*$1*")
      (str/replace #"--" "–")
      (str/replace #"<<" "«")
      (str/replace #">>" "»")
      (str/replace #"=([^=]+?)=" "`$1`")))

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
                    (str/join
                     "\n"
                     (for [{:keys [date id txt]}
                           (dreams-for-year-month yr mo dreams)]
                       (format "### %s\n\n%s"
                               (d/format-date-for-section date)
                               (org->md txt))))))))))))

(defn dreams-as-md [dreams]
  (let [toc (toc-str dreams)]
    (str toc "\n\n" (format-dreams dreams))))

(defn num-dreams []
  (->> dreams-path
       slurp
       parse-dreams
       count))

(defn format-single-dream [dream]
  (str/join
   "\n"
   [(format "# %s\n\n" (d/format-date-for-section (:date dream)))
    (org->md (:txt dream))]))

(defn random-dream-str []
  (let [dreams (->> dreams-path
                    slurp
                    parse-dreams)]
    (format-single-dream (rand-nth dreams))))

(defn spit-md []
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
