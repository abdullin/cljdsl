(ns cljdsl.genutil
  (:refer-clojure)
  (:require
   [clojure.java.io :as io]
   [clojure.string :as str]
   
   [clojure.java.shell :refer [sh]]
   ))


(defn- capitalize [^String s]
  (if (> (count s) 0)
    (str (Character/toUpperCase (.charAt s 0))
          (subs s 1))
    s))

(defn pascal-case [id]
  (str/join 
   (map capitalize 
        (str/split (str id) #"[\-/]"))))

(defn camel-case [id]
  (let [
        chunks (str/split (str id) #"[-/]")
        [h & t] chunks
        t (map str/capitalize t)
        ]
    (str/join (conj t (str/lower-case h)))))


(defn tab
  "Indents a code block by inserting :indent before lines"
  [& seq]
  (->> seq
       flatten
       (filter some?)
       (reduce
        (fn [agg next]
          (if (or (empty? agg) (= :n (peek agg)))
            (conj agg :t next)
            (conj agg next)))
        [])))


(defn body [& seq]
  (list " {" :n (tab seq) :n "}" :n))

(defn save [path content]
  (println "> " path)
  (assert (> 100 (count path)) (str "Long filename " path))
  
  (let [full (str path)]
    (io/make-parents full)
    (with-open [w (io/writer full)]
      (doseq [^String l content] (.write w l)))
    full))

(defn code->str
  "convert a sequence of symbols into a string"
  [seq]
  (for [x (flatten seq)]
    (cond
      (= :w x) " "
      (string? x) x
      (nil? x) ""
      (= :n x) "\n"
      (= :t x) "\t"
      (keyword? x) (name x) 
      :else (str x))))



(defn nice-sh [dir & args]
  (print   dir ">" (str/join " " args))
  (let [result (apply sh (concat args [:dir dir]))]
    (if (> 0 (:exit result))
      (print (:err result)))))
