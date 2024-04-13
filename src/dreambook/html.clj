(ns dreambook.html
  (:require [clojure.string :as str]))

(defn escape-syms [s]
  (-> s
      (str/replace #"&" "&amp;")
      (str/replace #"(?<!\S)\"([^\"]+)\"(?!\S)"
                   "&#8220;$1&#8221;")
      (str/replace "I'm" "I&#8217;m")
      (str/replace #"(?=\S)s'(?!\S)" "s&#8217;")
      (str/replace #"(?=\S)'s" "&#8217;s")
      ;; Italics:
      (str/replace #"(?<!\S)\*([^\*]+)\*([\.”\"]*)?(?!\S)" "<em>$1</em>$2")
      ;; Boldface:
      (str/replace #"(?<!\S)\*\*([^\*]+)\*\*([\.”\"]*)?(?!\S)" "<strong>$1</strong>$2")
      ;; Strikethrough:
      (str/replace #"\~\~([^\~]+)\~\~" "<s>$1</s>")
      (str/replace #"`([^`]+)`" "<code>$1</code>")))

(defn md->html
  [s]
  (let [paragraph-sections (str/split s #"\n\n")]
    (str/join
     "\n"
     (for [section paragraph-sections]
       (let [hpat (re-find #"^(#+)\s(.+?)$" section)]
         (if hpat
           (let [[_ h txt] hpat
                 hn (count h)]
             (str "<h" hn ">" (escape-syms txt) "</h" hn ">"))
           (str "<p>" (escape-syms section) "</p>")))))))
