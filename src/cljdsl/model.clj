(ns cljdsl.model
  (:refer-clojure :exclude [type])
  (:require
   [clojure.java.io :as io]
   [clojure.string :as str]
   [clojure.walk :refer [postwalk-replace]]
   )
  (:gen-class))


(defn- new-model [] { :counter 0 })
(def model (atom (new-model)))

(defn clear!
  "Remove all data from the model"
  [] (swap! model (fn [_] (new-model))))



(defn get-model [] @model)
(defn get-nodes [] (:nodes (get-model)))


(defn merge! [mp] (swap! model (fn [mod] (merge mod mp))))


(defn get-by-types
  "Get all nodes by their type(s)"
  [& filters]
  (let [h (apply hash-set filters)]
    (filter #(contains? h (:node-type %) ) (vals  (:nodes @model)))))

(defn get-by-id
  "Get node by id"
  [id]
  (get-in @model [:nodes id]))

(defn node-is? [node & filters]
  (let [h (apply hash-set filters)]
    (contains? h (:node-type node))))

(defn filter-ids-by-type
  "filter ids which belong to a specific type"
  [type coll]
  (filter #(-> % get-by-id :node-type (= type)) coll))

(defn- verify-node
  [{:keys [id node-type package doc] :as node}]
  (assert node-type (str "Node-type is required for " node))

  (assert id (str "id is required for " node)))


(defn- add-node-to-model
  "Inserts a model node into the model"
  [{:keys [counter] :as model} {:keys [id] :as node}]
  (verify-node node)
  (let [node (assoc node :seq counter)]
    (-> model
        (update-in [:nodes] assoc id node)
        (update-in [:counter] inc))))

(defn add!
  "Pushes node into the model and returns the node"
  [node]
  (if node (swap! model add-node-to-model node))
  node)



(defn- push!
  "Pushes the provided element into the model at a specified path"
  [element & path]
  (let [id (:id element)]
    (assert id "Element must have an id")
    (swap! model update-in path assoc id element)
    ;; for debugging assist, return result as if we pushed to an empty model
    (update-in (new-model) path assoc id element)))
