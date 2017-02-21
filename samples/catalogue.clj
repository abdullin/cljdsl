
(ns sample.catalogue
  (:refer-clojure :exclude [type alias set])
  (:require
   [cljdsl.lang :refer :all]))

(go-project "github.com/abdullin/cljdsl_catalogue")

(group "base" "Native bindings"
       (native long "Int64" "long")
       (native bool "Boolean" "bool")
       (native string "Unicode string" "string")
       (native int "Int32" "int")
       (native byte "256 bits" "byte")
       (native date "Date" "DateTime")
       (alias positive-long "Positive long" long (> 0)))

(group "tenant" "tenant-related schemas"
       (alias id "Numeric tenant id" long (> 0))
       (enum status "Enum status" byte [:new :active :suspended]))
