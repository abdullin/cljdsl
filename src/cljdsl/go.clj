(ns cljdsl.go
  (:refer-clojure :exclude [type])
  (:require

   [clojure.java.io :as io]
   [clojure.string :as str]
   
   [clojure.java.shell :refer [sh]]
   [cljdsl.model :as model]
   [cljdsl.genutil :as util]
   ) 
  (:gen-class))



(defn- public [id] (util/pascal-case id))
(defn- private [id] (util/camel-case id))

(defn root-path [m]
  (let [root (or (System/getenv "GOPATH") ".")]
    (str root "/src/" (get-in m [:go :root]) )))



(defn block
  ([x] (list "{" :n x :n "}" :n))
  ([x y] (list "{" :n x y :n "}" :n))
  ([x y & rest] (concat (list "{" :n x y) rest (list :n "}" :n) )))



(defn package [loc & body]
  (merge loc {:body (list "package " (:package loc) ";" :n body)}))


(defn go-loc-for-lang-type [id]
  (let [
        package (str (namespace id))
        nm  (str (name id) ".g.go")
        full (str "lang/" package "/" nm)
        ]
    {:package package
     :file nm
     :full full
     }))

(defn render-enum [{:keys [id type-ref fields]}]
  (let [loc (go-loc-for-lang-type id)
        title (public (name id))]
    (package loc
             "type " title " " type-ref :n
             "const (" :n
     (map-indexed (fn [i f] (list title "_" (public (name f)) " " title " = " (+ 1 i) :n)) fields)
     ")" :n
     )))


(defn gen-enums! []
  (for [enum (model/get-by-types :enum)]
    (render-enum enum)
    ))



(defn build!
  "invokes golang build for the project"
  [root]
  (util/nice-sh root "goimports" "-w" ".")
  
  ;;(print (:err (sh "goimports" "-w" "." :dir root)))
  ;(print (:err (sh "go" "build" "." :dir (str root "/host"))))
  (print "\033[0m")
  "Done")






(defn gen! []
  (let [ root (root-path (model/get-model) )
        files (concat (gen-enums!))]
    
      (println "Generating files in" root)
    (doseq [{:keys [body full]} files]
      
      (util/save (str root "/" full) 
                 
                 (util/code->str body))
      

      )
    (build! root)
    )


  )

