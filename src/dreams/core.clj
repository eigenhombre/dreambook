(ns dreams.core
  (:require
  [clojure.string :as str]
  [clojure.java.shell :as sh]))

(import '[java.time LocalDate]
        '[java.time.format DateTimeFormatter]
        '[java.util Locale])

(def org-dir (str (System/getenv "HOME") "/org"))
(def dreams-path (str org-dir "/dreams.org"))
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

(defn dream-years [dreams]
  (->> dreams
       (map :year)
       distinct))

(defn dreams-for-year [year dreams]
  (filter (comp #{year} #(.getYear %) :date)
          dreams))

(defn toc-str [dreams]
  (str/join
   "\n"
   (for [yr (dream-years dreams)]
     (str/join "\n" (concat [(format "- [%s](#%s)" yr yr)]
                            (for [dr (dreams-for-year yr dreams)]
                              (format "    - [%s](#%s)"
                                      (format-date (:date dr))
                                      (:id dr))))))))

(comment
  ;; Years
  (->> dreams-path
       slurp
       parse-dreams
       (map :year)
       distinct)
  ;;=>
  '(1998 2000 2002 2011 2017 2019 2021 2022 2023 2024)

  ;; Days
  (->> dreams-path
       slurp
       parse-dreams
       (map :date)
       count)
  ;;=>
  148

  ;; TOC
  (println
   (->> dreams-path
        slurp
        parse-dreams
        toc-str))
  )


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
  (let [years (dream-years dreams)]
    (str/join
     "\n\n"
     (for [yr years]
       (str (format "# <a name=\"%s\"></a>%s\n\n" yr yr)
            (str/join
             "\n"
             (for [{:keys [date id txt]} (dreams-for-year yr dreams)]
               (format "## <a name=\"%s\"></a>%s\n\n%s"
                       id
                       (format-date-for-section date)
                       txt))))))))

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

(defn -main []
  (let [dreams (->> dreams-path
                    slurp
                    parse-dreams)]
    (spit "/tmp/dreams.md" (dreams-as-md dreams))

    (println (sh/sh "ebook-convert"
                    "/tmp/dreams.md"
                    epub-output
                    "--output-profile=tablet"
                    (str "--cover=" cover-image)
                    "--authors=Eig N. Hombre"
                    "--title=eBook of Dreams"))

    (println (format "Saved %d dreams to %s."
                     (count dreams)
                     epub-output)))

  ;; (println (sh/sh "open" "-a" "/Applications/calibre.app" epub-output))

  )

(when (= *file* (System/getProperty "babashka.file"))
  (-main))
