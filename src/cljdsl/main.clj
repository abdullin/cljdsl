(ns cljdsl.main
  (:refer-clojure :exclude [type])
  (:require

   [clojure.java.io :as io]
   [clojure.string :as str]
   [cljdsl.model :refer [get-model add! get-nodes]]
   [cljfmt.core :as cljfmt]
   [cljdsl.lang :as lang]
   [cljdsl.go :as go]
   ) 
  (:gen-class))


(def dsl-indents (merge cljfmt/default-indents lang/indents))

(defn reformat [f]
  (let [original (slurp f)]
    (try
      (let [revised (cljfmt/reformat-string original {:indents dsl-indents})]
        (when (not= original revised)
          (println "Reformatting" f)
          (spit f revised)))
      (catch Exception e
        (println "Failed to format:" f)))))


(defn -main [& args]
  (println "DSL tool")

  (doseq [a args]
    (reformat a)
    (println "Loading " a)
    (load-file a))

  (println "Loaded" (count  (get-nodes)) "language elements" )
  (go/gen!)

;;  (generate/csharp!)
  
    ;; to shutdown any futures created by call to sh
  (shutdown-agents))
