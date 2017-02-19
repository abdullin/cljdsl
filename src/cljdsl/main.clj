(ns cljdsl.main
  (:refer-clojure :exclude [type])
  (:require

   [clojure.java.io :as io]
   [clojure.string :as str]
   [cljdsl.model :refer [get-model add! get-nodes]]
   [cljfmt.core :refer [reformat-string default-indents]]
   ) 
  (:gen-class))


(def dsl-indents (merge default-indents

{

 'space        [[:inner 0]]
 'group       [[:inner 1]]
 'lmdb [[:inner 0]]
 
 }

                        ))

(defn reformat [f]
  (let [original (slurp f)]
    (try
      (let [revised (reformat-string original {:indents dsl-indents})]
        (when (not= original revised)
          (println "Reformatting" f)
          (spit f revised)))
      (catch Exception e
        (println "Failed to format:" f)))))


(defn -main [& args]
  (println "DSL tool")
  (println dsl-indents)

  (doseq [a args]
    (reformat a)
    (println "Loading " a)
    
    (load-file a))

  (println (count  (get-nodes)))

;;  (generate/csharp!)
  
    ;; to shutdown any futures created by call to sh
  (shutdown-agents))
