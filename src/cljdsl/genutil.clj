(ns cljdsl.genutil
  (:refer-clojure)
  (:require

   [clojure.string :as str]))




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
